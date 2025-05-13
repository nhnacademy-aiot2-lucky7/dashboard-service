package com.nhnacademy.common.memory;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class DashboardMemoryTest {

    @BeforeEach
    void setUp() {
        DashboardMemory.addPanel("dashboard-uid", 1);
        DashboardMemory.addPanel("dashboard-uid", 2);
    }

    @Test
    @DisplayName("생성자 생성 금지")
    void create_constructor() throws NoSuchMethodException {
        Constructor<DashboardMemory> constructor = DashboardMemory.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        InvocationTargetException exception = assertThrows(InvocationTargetException.class, constructor::newInstance);

        assertInstanceOf(UnsupportedOperationException.class, exception.getCause());
        assertEquals("Utility class", exception.getCause().getMessage());
    }

    @Test
    @DisplayName("panelId가 null이지만 null 값도 추가되어 있었던 경우 제거 확인")
    void removePanelWithDashboardUid() {
        DashboardMemory.removePanel("dashboard-uid", 1);
        DashboardMemory.removePanel("dashboard-uid", 2);

        Set<Integer> panels = DashboardMemory.getPanels("dashboard-uid");
        Assertions.assertTrue(panels.isEmpty(), "dashboardUid는 제거되어야 하므로 panel 목록은 비어 있어야 함");

    }
}