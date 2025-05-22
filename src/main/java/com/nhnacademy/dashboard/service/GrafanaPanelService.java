package com.nhnacademy.dashboard.service;

import com.nhnacademy.common.memory.DashboardMemory;
import com.nhnacademy.dashboard.api.GrafanaApi;
import com.nhnacademy.dashboard.dto.dashboard.GrafanaCreateDashboardRequest;
import com.nhnacademy.dashboard.dto.dashboard.InfoDashboardResponse;
import com.nhnacademy.dashboard.dto.grafana.GrafanaMetaResponse;
import com.nhnacademy.dashboard.dto.panel.CreatePanelRequest;
import com.nhnacademy.dashboard.dto.panel.DeletePanelRequest;
import com.nhnacademy.dashboard.dto.panel.ReadPanelRequest;
import com.nhnacademy.dashboard.dto.panel.IframePanelResponse;
import com.nhnacademy.dashboard.dto.panel.UpdatePanelPriorityRequest;
import com.nhnacademy.dashboard.dto.panel.UpdatePanelRequest;
import com.nhnacademy.dashboard.dto.dashboard.json.Dashboard;
import com.nhnacademy.dashboard.dto.dashboard.json.Panel;
import com.nhnacademy.dashboard.dto.dashboard.json.Target;
import com.nhnacademy.dashboard.dto.user.UserDepartmentResponse;
import com.nhnacademy.dashboard.exception.NotFoundException;
import com.nhnacademy.event.event.EventCreateRequest;
import com.nhnacademy.event.rabbitmq.EventProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GrafanaPanelService {

    private final GrafanaApi grafanaApi;
    private final EventProducer eventProducer;
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

        UserDepartmentResponse departmentResponse = grafanaFolderService.getFolderTitle(userId);
        String departmentId = departmentResponse.getDepartmentId();
        String folderTitle = departmentResponse.getDepartmentName();
        GrafanaCreateDashboardRequest existDashboard = grafanaDashboardService.getDashboardInfo(request.getDashboardUid());

        String fluxQuery = grafanaDashboardService.generateFluxQuery(
                request.getBucket(),
                request.getMeasurement(),
                request.getSensorFieldRequestDto(),
                request.getAggregation(),
                request.getTime());

        // 이름이 중복된 경우
        String panelTitle = request.getPanelTitle();
        for (Panel panel : existDashboard.getDashboard().getPanels()) {
            if (panel.getTitle().equals(panelTitle)) {
                panelTitle = duplicatedName(panelTitle);
                break;
            }
        }

        GrafanaCreateDashboardRequest newDashboardRequest = grafanaDashboardService.buildDashboardRequest(
                userId,
                request.getGridPos(),
                request.getType(),
                existDashboard.getDashboard().getTitle(),
                panelTitle,
                fluxQuery);

        Dashboard newDashboard = grafanaDashboardService.buildDashboard(newDashboardRequest);

        // 기존 패널과 합쳐서 구성
        List<Panel> panels = new ArrayList<>(existDashboard.getDashboard().getPanels());
        panels.addAll(newDashboard.getPanels());
        newDashboard.setPanels(panels);

        GrafanaCreateDashboardRequest finalRequest = new GrafanaCreateDashboardRequest();
        finalRequest.setDashboard(newDashboard);
        finalRequest.setFolderUid(grafanaFolderService.getFolderUidByTitle(folderTitle));
        finalRequest.setOverwrite(true);

        log.info("CREATE Panel -> request: {}", finalRequest);
        ResponseEntity<GrafanaMetaResponse> response = grafanaApi.updateDashboard(finalRequest);
        log.info("CREATE Panel result: {}", response.toString());

        Set<Integer> panelIds = newDashboard.getPanels()
                .stream()
                .map(Panel::getId)
                .collect(Collectors.toSet());

        panelIds.forEach(panelId ->
                DashboardMemory.addPanel(finalRequest.getDashboard().getUid(), panelId));

        log.info("panelId size:{}", panelIds.size());

        EventCreateRequest event = new EventCreateRequest(
                "INFO",
                "패널 생성",
                Integer.toString(panelIds.size()),
                departmentId,
                LocalDateTime.now()
        );
        eventProducer.sendEvent(event);
    }

    /**
     * 중복된 차트 이름에 대해 번호를 붙여 새로운 이름을 생성합니다.
     *
     * @param name 기존 이름
     * @return 중복되지 않는 새로운 이름
     */
    public String duplicatedName(String name) {
        int index = 1;
        String baseTitle = name;

        if (name.contains("(")) {
            String[] parts = name.split("\\(");
            baseTitle = parts[0];

            String numberPart = parts[1].replace(")", "");
            try {
                index = Integer.parseInt(numberPart) + 1; // 숫자 + 1
            } catch (NumberFormatException e) {
                log.info("NumberFormatException: {}", e.getMessage());
            }
        } else {
            Pattern pattern = Pattern.compile("^(\\D*?)(\\d+)$");
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

        GrafanaCreateDashboardRequest dashboard = grafanaDashboardService.getDashboardInfo(readPanelRequest.getDashboardUid());

        List<Panel> panels = dashboard.getDashboard().getPanels();

        boolean allIdsNull = panels.stream().allMatch(p -> p.getId() == null);
        if (allIdsNull) {
            throw new NotFoundException("panel not found for uid: " + readPanelRequest.getDashboardUid());
        }

        List<IframePanelResponse> responseList = panels.stream()
                .map(panel -> IframePanelResponse.ofNewIframeResponse(
                        dashboard.getDashboard().getUid(),
                        dashboard.getDashboard().getTitle(),
                        panel.getId()))
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

        GrafanaCreateDashboardRequest dashboard = grafanaDashboardService.getDashboardInfo(dashboardUid);
        List<Panel> panel = dashboard.getDashboard().getPanels();

        if (panel.isEmpty()) {
            throw new NotFoundException("Uid에 해당하는 패널이 없습니다: " + dashboardUid);
        }

        log.info("panelId:{}", panel.size());
        return panel.stream()
                .filter(p -> !offPanelId.contains(p.getId()))
                .map(p -> IframePanelResponse.ofNewIframeResponse(dashboardUid, dashboard.getDashboard().getTitle(), p.getId()))
                .toList();
    }

    /**
     * 기존 대시보드에서 특정 패널의 정보를 수정합니다.
     * - 제목, 차트 타입, 쿼리를 수정하며 기존 대시보드를 overwrite합니다.
     *
     * @param userId  사용자 ID
     * @param request 패널 수정 요청 정보
     */
    public void updatePanel(String userId, UpdatePanelRequest request) {

        String folderUid = grafanaFolderService.getFolderUidByTitle(grafanaFolderService.getFolderTitle(userId).getDepartmentName());
        GrafanaCreateDashboardRequest existDashboard = grafanaDashboardService.getDashboardInfo(request.getDashboardUid());
        String fluxQuery = grafanaDashboardService.generateFluxQuery(
                request.getBucket(),
                request.getMeasurement(),
                request.getSensorFieldRequestDto(),
                request.getAggregation(),
                request.getTime());

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

        log.info("UPDATE CHART -> First panel query: {}",
                dashboardRequest.getDashboard().getPanels().getFirst().getTargets().getFirst().getQuery());
        ResponseEntity<GrafanaMetaResponse> respsonse = grafanaApi.updateDashboard(dashboardRequest);
        log.info("UPDATE result: {}", respsonse.toString());

        String departmentId = grafanaFolderService.getFolderTitle(userId).getDepartmentId();
        EventCreateRequest event = new EventCreateRequest(
                "INFO",
                "패널 수정",
                existDashboard.getDashboard().getPanels().getFirst().getId().toString(),
                departmentId,
                LocalDateTime.now()
        );
        eventProducer.sendEvent(event);
    }

    /**
     * 요청된 패널 ID 순서에 따라 각 차트의 우선순위를 재배치합니다.
     *
     * @param userId                     사용자 ID
     * @param updatePanelPriorityRequest 패널 우선순위 정보가 담긴 요청 객체
     * @throws NotFoundException 요청된 패널 ID가 존재하지 않을 경우
     */
    public void updatePriority(String userId, UpdatePanelPriorityRequest updatePanelPriorityRequest) {

        UserDepartmentResponse userDepartmentResponse = grafanaFolderService.getFolderTitle(userId);
        String folderTitle = userDepartmentResponse.getDepartmentName();
        String folderUid = grafanaFolderService.getFolderUidByTitle(folderTitle);
        GrafanaCreateDashboardRequest existDashboard = grafanaDashboardService.getDashboardInfo(updatePanelPriorityRequest.getDashboardUid());
        List<Panel> originalPanels = existDashboard.getDashboard().getPanels();

        List<Panel> sortedPanels = updatePanelPriorityRequest.getPanelPriority().stream()
                .map(id -> originalPanels.stream()
                        .filter(p -> p.getId().equals(id))
                        .findFirst()
                        .orElseThrow(() -> new NotFoundException("해당 panelId가 없습니다.")))
                        .toList();

        existDashboard.getDashboard().setPanels(sortedPanels);
        GrafanaCreateDashboardRequest dashboardRequest = overwritten(existDashboard, sortedPanels, folderUid);

        grafanaApi.updateDashboard(dashboardRequest);

        EventCreateRequest event = new EventCreateRequest(
                "INFO",
                "패널 우선순위 변경",
                sortedPanels.getFirst().toString(),
                userDepartmentResponse.getDepartmentId(),
                LocalDateTime.now()
        );
        eventProducer.sendEvent(event);
    }

    /**
     * 기존 대시보드 정보를 기반으로 새로운 패널 리스트와 폴더 UID를 설정하여
     * overwrite 옵션이 적용된 대시보드 요청 객체를 생성합니다.
     *
     * @param existDashboard 기존 대시보드 정보
     * @param panels         갱신된 패널 리스트
     * @param folderUid      폴더 UID
     * @return 대시보드 요청 객체 (overwrite 포함)
     */
    public GrafanaCreateDashboardRequest overwritten(GrafanaCreateDashboardRequest existDashboard, List<Panel> panels, String folderUid) {
        GrafanaCreateDashboardRequest dashboardRequest = new GrafanaCreateDashboardRequest();
        Dashboard dashboard = new Dashboard(
                existDashboard.getDashboard().getId(),
                existDashboard.getDashboard().getTitle(),
                existDashboard.getDashboard().getUid(),
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
    public void removePanel(String userId,DeletePanelRequest deletePanelRequest) {
        GrafanaCreateDashboardRequest existDashboard = grafanaDashboardService.getDashboardInfo(deletePanelRequest.getDashboardUid());
        List<Panel> panels = new ArrayList<>(existDashboard.getDashboard().getPanels());
        panels.removeIf(p -> p.getId().equals(deletePanelRequest.getPanelId()));

        InfoDashboardResponse dashboardInfoResponse = grafanaDashboardService.getDashboardInfoRequest(userId, existDashboard.getDashboard().getTitle());

        Dashboard dashboard = grafanaDashboardService.buildDashboard(existDashboard);
        dashboard.setPanels(panels);

        existDashboard.setDashboard(dashboard);
        existDashboard.setFolderUid(dashboardInfoResponse.getFolderUid());
        existDashboard.setOverwrite(true);

        ResponseEntity<GrafanaMetaResponse> response = grafanaApi.updateDashboard(existDashboard);
        log.info("REMOVE Panel result : {}", response.toString());

        DashboardMemory.removePanel(deletePanelRequest.getDashboardUid(), deletePanelRequest.getPanelId());

        String departmentId = grafanaFolderService.getFolderTitle(userId).getDepartmentId();
        EventCreateRequest event = new EventCreateRequest(
                "INFO",
                "패널 삭제",
                dashboard.getPanels().getFirst().getId().toString(),
                departmentId,
                LocalDateTime.now()
        );
        eventProducer.sendEvent(event);
    }
}
