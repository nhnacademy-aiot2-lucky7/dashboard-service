package com.nhnacademy.common.memory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

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
    @DisplayName("panelId가 null -> dashboardUid 제거 확인")
    void removePanelWithDashboardUid() {
        DashboardMemory.removePanel("dashboard-uid", 1);
        DashboardMemory.removePanel("dashboard-uid", 2);

        assertTrue(DashboardMemory.getPanels("dashboard-uid").isEmpty());
    }
}