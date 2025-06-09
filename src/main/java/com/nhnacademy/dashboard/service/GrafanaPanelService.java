package com.nhnacademy.dashboard.service;

import com.nhnacademy.common.memory.DashboardMemory;
import com.nhnacademy.dashboard.api.GrafanaApi;
import com.nhnacademy.dashboard.dto.dashboard.DashboardBuildRequest;
import com.nhnacademy.dashboard.dto.dashboard.GrafanaCreateDashboardRequest;
import com.nhnacademy.dashboard.dto.dashboard.InfoDashboardResponse;
import com.nhnacademy.dashboard.dto.dashboard.json.FieldConfig;
import com.nhnacademy.dashboard.dto.grafana.GrafanaMetaResponse;
import com.nhnacademy.dashboard.dto.panel.*;
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
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class GrafanaPanelService {

    private final GrafanaApi grafanaApi;
    private final EventProducer eventProducer;
    private final GrafanaFolderService grafanaFolderService;
    private final GrafanaDashboardService grafanaDashboardService;
    private static final String PANEL_SOURCE_TYPE = "panel";

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
        log.info("Exist dashboard: {}", existDashboard.getDashboard().getTitle());

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

        DashboardBuildRequest dashboardBuildRequest = new DashboardBuildRequest(
                userId,
                request.getGridPos(),
                request.getType(),
                existDashboard.getDashboard().getTitle(),
                panelTitle,
                fluxQuery,
                request.getMin(),
                request.getMax()
        );
        GrafanaCreateDashboardRequest newDashboardRequest = grafanaDashboardService.buildDashboardRequest(dashboardBuildRequest);

        Dashboard newDashboard = grafanaDashboardService.buildDashboard(newDashboardRequest);
        log.info("새로운 대시보드 -> {}", newDashboard.getPanels().getFirst().getTitle());

        // 기존 패널과 합쳐서 구성
        List<Panel> panels = new ArrayList<>(existDashboard.getDashboard().getPanels());
        panels.addAll(newDashboard.getPanels());
        newDashboard.setPanels(panels);

        GrafanaCreateDashboardRequest finalRequest = new GrafanaCreateDashboardRequest();
        finalRequest.setDashboard(newDashboard);
        finalRequest.setFolderUid(grafanaFolderService.getFolderUidByTitle(folderTitle));
        finalRequest.setOverwrite(true);

        log.info("CREATE Panel query -> request: {}", finalRequest.getDashboard().getPanels().getFirst().getTargets().getFirst().getQuery());
        ResponseEntity<GrafanaMetaResponse> response = grafanaApi.updateDashboard(finalRequest);
        log.info("CREATE Panel result: {}", response.getBody());

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
                PANEL_SOURCE_TYPE,
                "CREATE",
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
        int panelSize = panels.size();
        log.info("panelSize:{}", panelSize);

        Set<Integer> panelIds = DashboardMemory.getPanels(readPanelRequest.getDashboardUid());
        if (panelIds.isEmpty()) {
            log.warn("No panel IDs found for dashboard UID: " + readPanelRequest.getDashboardUid());
        }

        // panelIds를 List로 변환 (인덱스를 사용하기 위함)
        List<Integer> panelIdsList = new ArrayList<>(panelIds);

        log.info("panelIdsList:{}", panelIdsList);
        // panelIds와 panels의 크기가 맞지 않으면 예외 처리
        if (panelIdsList.size() != panels.size()) {
            log.warn("Mismatch between panelIds size and panels size. panelIds size: " + panelIdsList.size() + ", panels size: " + panels.size());
        }

        List<IframePanelResponse> responseList = IntStream.range(0, panels.size())
                .mapToObj(index -> {
                    Panel panel = panels.get(index);
                    int panelId = panelIdsList.get(index);
                    return IframePanelResponse.ofNewIframeResponse(
                            dashboard.getDashboard().getUid(),
                            dashboard.getDashboard().getTitle(),
                            panelId,
                            panel.getGridPos().getW(),
                            panel.getGridPos().getH(),
                            panel.getType(),
                            panel.getTitle(),
                            panel.getTargets().getFirst().getQuery(),
                            panel.getFieldConfig().getDefaults().getThresholds().getSteps()
                            );
                })
                .collect(Collectors.toList());

        // 응답 반환
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
                .map(p -> IframePanelResponse.ofNewIframeResponse(dashboardUid, dashboard.getDashboard().getTitle(), p.getId(), p.getGridPos().getW(), p.getGridPos().getH(),p.getType(), p.getTitle(), p.getTargets().getFirst().getQuery(), p.getFieldConfig().getDefaults().getThresholds().getSteps()))
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

        log.info("수정할 panelId:{}", request.getPanelId());
        String departmentName = grafanaFolderService.getFolderTitle(userId).getDepartmentName();
        String folderUid = grafanaFolderService.getFolderUidByTitle(departmentName);

        GrafanaCreateDashboardRequest existDashboard = grafanaDashboardService.getDashboardInfo(request.getDashboardUid());
        log.info("기존 Panel Name: {}", existDashboard.getDashboard().getPanels().getFirst().getTitle());
        log.info("기존 Panel Id: {}", existDashboard.getDashboard().getPanels().getFirst().getId());

        List<Panel> panels = existDashboard.getDashboard().getPanels();
        String fluxQuery = grafanaDashboardService.generateFluxQuery(
                request.getBucket(),
                request.getMeasurement(),
                request.getSensorFieldRequestDto(),
                request.getAggregation(),
                request.getTime());

        updateMatchingPanel(panels, request, fluxQuery);

        GrafanaCreateDashboardRequest dashboardRequest = overwritten(existDashboard, panels, folderUid);

        log.info("UPDATE QUERY:{}", dashboardRequest.getDashboard().getPanels().getFirst().getTargets().get(request.getPanelId()).getQuery());
        ResponseEntity<GrafanaMetaResponse> respsonse = grafanaApi.updateDashboard(dashboardRequest);
        log.info("UPDATE result: {}", respsonse.getBody());

        sendUpdateEvent(userId);
    }

    private void updateMatchingPanel(List<Panel> panels, UpdatePanelRequest request, String fluxQuery) {
        for (Panel panel : panels) {
            if (panel.getId().equals(request.getPanelId())) {
                panel.setTitle(request.getPanelNewTitle());
                panel.setType(request.getType());
                updateThresholds(panel, request);
                updatePanelQueries(panel, fluxQuery);
            }
        }
    }

    Optional<FieldConfig.Step> findStepByColor(List<FieldConfig.Step> steps, String color) {
        return steps.stream()
                .filter(step -> color.equals(step.getColor()))
                .findFirst();
    }

    private void updateThresholds(Panel panel, UpdatePanelRequest request) {
        if (panel.getFieldConfig() == null) return;

        List<FieldConfig.Step> steps = panel.getFieldConfig().getDefaults().getThresholds().getSteps();
        if (request.getMin() != null && !steps.isEmpty()) {
            findStepByColor(steps, "#EAB839").ifPresent(step -> step.setValue(request.getMin()));
            log.info("최소값 적용확인:{}", steps.getFirst().getValue());
        }
        if (request.getMax() != null && steps.size() > 1) {
            findStepByColor(steps, "red").ifPresent(step -> step.setValue(request.getMax()));
            log.info("최대값 적용확인:{}", steps.getLast().getValue());
        }
    }

    private void updatePanelQueries(Panel panel, String query) {
        if (panel.getTargets() == null) return;
        for (Target target : panel.getTargets()) {
            target.setQuery(query);
        }
    }

    private void sendUpdateEvent(String userId) {
        String departmentId = grafanaFolderService.getFolderTitle(userId).getDepartmentId();
        EventCreateRequest event = new EventCreateRequest(
                "INFO",
                "패널 수정",
                PANEL_SOURCE_TYPE,
                "UPDATE",
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
                PANEL_SOURCE_TYPE,
                "UPDATE_PRIORITY",
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
        log.info("패널 삭제 시도: dashboardUid={}, panelId={}", deletePanelRequest.getDashboardUid(), deletePanelRequest.getPanelId());

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
        log.info("REMOVE Panel result : {}", response.getBody());

        DashboardMemory.removePanel(deletePanelRequest.getDashboardUid(), deletePanelRequest.getPanelId());

        String departmentId = grafanaFolderService.getFolderTitle(userId).getDepartmentId();
        EventCreateRequest event = new EventCreateRequest(
                "INFO",
                "패널 삭제",
                PANEL_SOURCE_TYPE,
                "DELETE",
                departmentId,
                LocalDateTime.now()
        );
        eventProducer.sendEvent(event);
    }
}
