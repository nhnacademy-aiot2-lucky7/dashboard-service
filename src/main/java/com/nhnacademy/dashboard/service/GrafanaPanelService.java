package com.nhnacademy.dashboard.service;

import com.nhnacademy.dashboard.api.GrafanaApi;
import com.nhnacademy.dashboard.dto.dashboard.GrafanaCreateDashboardRequest;
import com.nhnacademy.dashboard.dto.panel.CreatePanelRequest;
import com.nhnacademy.dashboard.dto.panel.DeletePanelRequest;
import com.nhnacademy.dashboard.dto.panel.ReadPanelRequest;
import com.nhnacademy.dashboard.dto.panel.IframePanelResponse;
import com.nhnacademy.dashboard.dto.panel.UpdatePanelPriorityRequest;
import com.nhnacademy.dashboard.dto.panel.UpdatePanelRequest;
import com.nhnacademy.dashboard.dto.dashboard.json.Dashboard;
import com.nhnacademy.dashboard.dto.dashboard.json.GridPos;
import com.nhnacademy.dashboard.dto.dashboard.json.Panel;
import com.nhnacademy.dashboard.dto.dashboard.json.Target;
import com.nhnacademy.dashboard.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class GrafanaPanelService {

    private final GrafanaApi grafanaApi;
    private final GrafanaFolderService grafanaFolderService;
    private final GrafanaDashboardService grafanaDashboardService;

    /**
     * 주어진 정보에 따라 Grafana에 차트를 생성합니다.
     * - 기존 대시보드가 비어있을 경우 새로 생성합니다.
     * - 차트 제목이 중복될 경우 번호를 붙여 구분합니다.
     *
     * @param userId  사용자 ID
     * @param request 차트 생성 요청 정보
     */
    public void createPanel(String userId, CreatePanelRequest request) {

        String folderTitle = grafanaFolderService.getFolderTitle(userId);
        GrafanaCreateDashboardRequest dashboardRequest = new GrafanaCreateDashboardRequest();
        GrafanaCreateDashboardRequest existDashboard = grafanaDashboardService.getDashboardInfo(folderTitle);

        // 패널이 존재하지 않는 경우
        if (existDashboard.getDashboard().getPanels().isEmpty()) {
            String fluxQuery = grafanaDashboardService.generateFluxQuery(request.getSensorFieldRequestDto(), request.getAggregation(), request.getTime());
            GrafanaCreateDashboardRequest buildDashboardRequest = grafanaDashboardService.buildDashboardRequest(
                    userId,
                    request.getGridPos(),
                    request.getType(),
                    existDashboard.getDashboard().getTitle(),
                    request.getPanelTitle(),
                    fluxQuery);

            Dashboard dashboard = grafanaDashboardService.getDashboard(buildDashboardRequest);

            dashboardRequest.setDashboard(dashboard);
            dashboardRequest.setFolderUid(grafanaFolderService.getFolderUidByTitle(folderTitle));
            dashboardRequest.setOverwrite(true);

            log.info("CREATE CHART -> request: {}", dashboardRequest);

            grafanaApi.updateDashboard(dashboardRequest).getBody();
        }

        String fluxQuery = grafanaDashboardService.generateFluxQuery(request.getSensorFieldRequestDto(), request.getAggregation(), request.getTime());

        // 이름이 중복된 경우
        if (request.getPanelTitle().equals(existDashboard.getDashboard().getPanels().getFirst().getTitle())) {
            String newTitle = sameName(request.getPanelTitle());
            request.setPanelTitle(newTitle);
        }

        GrafanaCreateDashboardRequest buildDashboardRequest = grafanaDashboardService.buildDashboardRequest(
                userId,
                request.getGridPos(),
                request.getType(),
                existDashboard.getDashboard().getTitle(),
                request.getPanelTitle(),
                fluxQuery);

        List<Panel> panels = existDashboard.getDashboard().getPanels();
        panels.addAll(buildDashboardRequest.getDashboard().getPanels());
        Dashboard dashboard = grafanaDashboardService.getDashboard(buildDashboardRequest);
        dashboard.setPanels(panels);

        dashboardRequest.setDashboard(dashboard);
        dashboardRequest.setFolderUid(grafanaFolderService.getFolderUidByTitle(folderTitle));
        dashboardRequest.setOverwrite(true);

        log.info("CREATE CHART -> request: {}", dashboardRequest);

        grafanaApi.updateDashboard(dashboardRequest);
    }

    /**
     * 중복된 차트 이름에 대해 번호를 붙여 새로운 이름을 생성합니다.
     *
     * @param name 기존 이름
     * @return 중복되지 않는 새로운 이름
     */
    public String sameName(String name) {
        int index = 1;
        String baseTitle = name;

        int lastOpen = name.lastIndexOf('(');
        int lastClose = name.lastIndexOf(')');

        if (lastOpen != -1 && lastClose == name.length() - 1 && !name.matches(".*\\d+.*")) {
            String numberPart = name.substring(lastOpen + 1, lastClose);
            try {
                index = Integer.parseInt(numberPart) + 1;
                baseTitle = name.substring(0, lastOpen);
            } catch (NumberFormatException e) {
                // 숫자가 아닌 경우는 무시하고 index = 1, baseTitle = name 유지
                log.info(e.getMessage());
            }
        }else {
            Pattern pattern = Pattern.compile("(.*?)(\\d+)$");
            Matcher matcher = pattern.matcher(name);
            if (matcher.find()) {
                baseTitle = matcher.group(1);
                try {
                    index = Integer.parseInt(matcher.group(2)) + 1;
                } catch (NumberFormatException e) {
                    log.info(e.getMessage());
                }
            }
        }

        return String.format("%s(%d)", baseTitle, index);
    }

    /**
     * 특정 대시보드 UID에 해당하는 모든 패널 정보를 Iframe 형식으로 반환합니다.
     *
     * @param readPanelRequest 대시보드 UID를 포함한 요청
     * @return Iframe 패널 응답 목록
     * @throws NotFoundException 대시보드 UID가 존재하지 않을 경우
     */

    public List<IframePanelResponse> getPanel(ReadPanelRequest readPanelRequest) {

        GrafanaCreateDashboardRequest dashboard = grafanaApi.getDashboardInfo(readPanelRequest.getDashboardUid());
        if (dashboard == null) {
            throw new NotFoundException("존재하지 않는 uid : "+ readPanelRequest.getDashboardUid());
        }

        List<Panel> panels = dashboard.getDashboard().getPanels();
        List<IframePanelResponse> responseList = panels.stream()
                .map(panel -> IframePanelResponse.ofNewIframeResponse(
                        dashboard.getDashboard().getUid(),
                        dashboard.getDashboard().getTitle(),
                        panel.getId(),
                        readPanelRequest.getFrom()))
                .toList();

        return ResponseEntity.ok(responseList).getBody();
    }


    /**
     * 필터 조건에 따라 특정 대시보드에서 표시할 차트만 선택하여 반환합니다.
     *
     * @param dashboardUid 대시보드 UID
     * @return Iframe 응답 패널 목록
     * @throws NotFoundException 패널이 없을 경우
     */
    public List<IframePanelResponse> getFilterPanel(
            String dashboardUid,
            List<Integer> offPanelId) {

        GrafanaCreateDashboardRequest dashboard = grafanaApi.getDashboardInfo(dashboardUid);
        List<Panel> panel = dashboard.getDashboard().getPanels();

        if (panel == null) {
            throw new NotFoundException("panel not found for uid: " + dashboardUid);
        }

        return panel.stream()
                .filter(p -> !offPanelId.contains(p.getId()))
                .map(p -> IframePanelResponse.ofNewIframeResponse(dashboardUid, dashboard.getDashboard().getTitle(), p.getId()))
                .toList();
    }

    /**
     * 기존 대시보드에서 특정 패널의 정보를 수정합니다.
     * - 제목, 차트 타입, 쿼리를 수정하며 기존 대시보드를 overwrite합니다.
     *
     * @param userId 사용자 ID
     * @param request 패널 수정 요청 정보
     */
    public void updatePanel(String userId, UpdatePanelRequest request) {

        String folderUid = grafanaFolderService.getFolderUidByTitle(grafanaFolderService.getFolderTitle(userId));
        GrafanaCreateDashboardRequest existDashboard = grafanaDashboardService.getDashboardInfo(request.getDashboardUid());
        String fluxQuery = grafanaDashboardService.generateFluxQuery(request.getSensorFieldRequestDto(), request.getAggregation(), request.getTime());

        List<Panel> panels = existDashboard.getDashboard().getPanels();
        for (Panel panel : panels) {
            if (panel.getId().equals(request.getPanelId())) {
                panel.setTitle(request.getPanelNewTitle());
                panel.setType(request.getType());

                if (panel.getTargets() != null) {
                    for (Target target : panel.getTargets()) {
                        target.setQuery(fluxQuery);
                    }
                }
            }
        }

        GrafanaCreateDashboardRequest dashboardRequest = overwritten(existDashboard, panels, folderUid);

        log.info("UPDATE CHART -> request: {}", fluxQuery);
        grafanaApi.updateDashboard(dashboardRequest);
    }

    /**
     * 요청된 패널 ID 순서에 따라 각 차트의 우선순위를 재배치합니다.
     *
     * @param userId 사용자 ID
     * @param updatePanelPriorityRequest 패널 우선순위 정보가 담긴 요청 객체
     * @throws NotFoundException 요청된 패널 ID가 존재하지 않을 경우
     */
    public void updatePriority(String userId, UpdatePanelPriorityRequest updatePanelPriorityRequest){
        String folderTitle = grafanaFolderService.getFolderTitle(userId);
        String folderUid = grafanaFolderService.getFolderUidByTitle(folderTitle);
        GrafanaCreateDashboardRequest existDashboard = grafanaDashboardService.getDashboardInfo(updatePanelPriorityRequest.getDashboardUid());
        List<Panel> panels = existDashboard.getDashboard().getPanels();

        int yPos = 0;
        for(Integer targetPanelId : updatePanelPriorityRequest.getDashboardPriority()){
            Panel panel = panels.stream()
                    .filter(p -> p.getId().equals(targetPanelId))
                    .findFirst()
                    .orElseThrow(()-> new NotFoundException("해당 panelId가 없습니다."));

            GridPos gridPos = panel.getGridPos();
            if (gridPos == null) {
                gridPos = new GridPos();
                panel.setGridPos(gridPos);
            }
            gridPos.setX(0);
            gridPos.setY(yPos);
            gridPos.setW(panel.getGridPos().getW());
            gridPos.setH(panel.getGridPos().getH());

            yPos += panel.getGridPos().getH();
        }

        GrafanaCreateDashboardRequest dashboardRequest = overwritten(existDashboard, panels, folderUid);


        grafanaApi.updateDashboard(dashboardRequest);
    }

    /**
     * 기존 대시보드 정보를 기반으로 새로운 패널 리스트와 폴더 UID를 설정하여
     * overwrite 옵션이 적용된 대시보드 요청 객체를 생성합니다.
     *
     * @param existDashboard 기존 대시보드 정보
     * @param panels 갱신된 패널 리스트
     * @param folderUid 폴더 UID
     * @return 대시보드 요청 객체 (overwrite 포함)
     */
    public GrafanaCreateDashboardRequest overwritten(GrafanaCreateDashboardRequest existDashboard, List<Panel> panels, String folderUid) {
        GrafanaCreateDashboardRequest dashboardRequest = new GrafanaCreateDashboardRequest();
        Dashboard dashboard = new Dashboard(
                existDashboard.getDashboard().getId(),
                existDashboard.getDashboard().getUid(),
                existDashboard.getDashboard().getTitle(),
                panels
        );

        dashboardRequest.setDashboard(dashboard);
        dashboardRequest.setFolderUid(folderUid);
        dashboardRequest.setOverwrite(true);

        return dashboardRequest;
    }

    /**
     * 요청된 제목에 해당하는 패널(차트)을 대시보드에서 제거하고 업데이트합니다.
     *
     * @param deletePanelRequest 삭제할 패널 정보를 담은 요청 객체
     */
    public void removePanel(DeletePanelRequest deletePanelRequest) {
        GrafanaCreateDashboardRequest existDashboard = grafanaDashboardService.getDashboardInfo(deletePanelRequest.getDashboardUid());
        List<Panel> panels = existDashboard.getDashboard().getPanels();
        panels.removeIf(p -> p.getId().equals(deletePanelRequest.getPanelId()));

        Dashboard dashboard = grafanaDashboardService.getDashboard(existDashboard);
        dashboard.setPanels(panels);

        existDashboard.setDashboard(dashboard);
        existDashboard.setFolderUid(existDashboard.getFolderUid());
        existDashboard.setOverwrite(true);

        grafanaApi.updateDashboard(existDashboard).getBody();
    }
}
