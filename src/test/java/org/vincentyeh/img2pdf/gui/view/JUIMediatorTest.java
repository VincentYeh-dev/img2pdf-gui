package org.vincentyeh.img2pdf.gui.view;

import org.assertj.swing.core.BasicRobot;
import org.assertj.swing.core.Robot;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.vincentyeh.img2pdf.lib.image.ColorType;
import org.vincentyeh.img2pdf.lib.pdf.parameter.PageAlign;
import org.vincentyeh.img2pdf.lib.pdf.parameter.PageDirection;
import org.vincentyeh.img2pdf.lib.pdf.parameter.PageSize;

import javax.swing.*;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;

import org.vincentyeh.img2pdf.gui.model.Task;

import static org.assertj.core.api.Assertions.assertThat;

@TestMethodOrder(MethodOrderer.MethodName.class)
class JUIMediatorTest {

    private Robot robot;
    private FrameFixture window;
    private UIMediator mediator;

    @BeforeEach
    void setUp() {
        UIState.resetForTesting();
        robot = BasicRobot.robotWithNewAwtHierarchy();
        JFrame frame = GuiActionRunner.execute(() -> {
            View view = new View();
            mediator = view.getUIMediator();
            mediator.initialize();
            JFrame f = new JFrame("Test");
            f.add(view.getRootPanel());
            f.pack();
            return f;
        });
        window = new FrameFixture(robot, frame);
        window.show();
        robot.waitForIdle();
    }

    @AfterEach
    void tearDown() {
        window.cleanUp();
    }

    @Test
    void check_encrypt_enables_password_fields() {
        window.checkBox("encryptCheckBox").check();
        window.textBox("ownerPasswordField").requireEnabled();
        window.textBox("userPasswordField").requireEnabled();
    }

    @Test
    void uncheck_encrypt_disables_and_clears_password_fields() {
        // Enable encryption then type a password
        GuiActionRunner.execute(() -> mediator.notifyUI("encryption_change", true));
        window.textBox("ownerPasswordField").enterText("secret");

        // Disable encryption → fields should be disabled and cleared
        GuiActionRunner.execute(() -> mediator.notifyUI("encryption_change", false));
        window.textBox("ownerPasswordField").requireDisabled();
        window.textBox("userPasswordField").requireDisabled();
        window.textBox("ownerPasswordField").requireEmpty();
    }

    @Test
    void check_autoRotate_disables_direction_combo() {
        window.checkBox("autoRotateCheckBox").check();
        window.comboBox("directionComboBox").requireDisabled();
    }

    @Test
    void uncheck_autoRotate_enables_direction_combo() {
        GuiActionRunner.execute(() -> mediator.notifyUI("auto_rotate_change", true));
        window.comboBox("directionComboBox").requireDisabled();

        GuiActionRunner.execute(() -> mediator.notifyUI("auto_rotate_change", false));
        window.comboBox("directionComboBox").requireEnabled();
    }

    @Test
    void depend_on_img_page_size_disables_align_combos() {
        GuiActionRunner.execute(() ->
                mediator.notifyUI("page_size_change", PageSize.DEPEND_ON_IMG));
        window.comboBox("horizontalAlignComboBox").requireDisabled();
        window.comboBox("verticalAlignComboBox").requireDisabled();
    }

    @Test
    void set_running_true_disables_convert_and_enables_stop() {
        GuiActionRunner.execute(() -> mediator.setRunningState(true));
        window.button("convertButton").requireDisabled();
        window.button("stopButton").requireEnabled();
    }

    @Test
    void set_running_false_enables_convert_and_disables_stop() {
        GuiActionRunner.execute(() -> mediator.setRunningState(false));
        window.button("convertButton").requireEnabled();
        window.button("stopButton").requireDisabled();
    }

    @Test
    void output_folder_field_is_not_editable() {
        window.textBox("outputFolderField").requireNotEditable();
    }

    @Test
    void output_folder_initial_value_is_absolute_path() {
        String text = window.textBox("outputFolderField").text();
        assertThat(text).isNotEqualTo(".");
        assertThat(new File(text).isAbsolute()).isTrue();
    }

    @Test
    void output_folder_change_event_updates_uistate() {
        GuiActionRunner.execute(() -> mediator.notifyUI("output_folder_change", "output"));
        assertThat(UIState.getInstance().getDestinationFolder())
                .isEqualTo(new File("output"));
    }

    // ===== A. 初始狀態 =====

    @Test
    void initial_convert_button_is_disabled_without_tasks() {
        window.button("convertButton").requireDisabled();
    }

    @Test
    void initial_stop_button_is_disabled() {
        window.button("stopButton").requireDisabled();
    }

    @Test
    void initial_clear_all_button_is_enabled() {
        window.button("clearAllButton").requireEnabled();
    }

    @Test
    void initial_source_browse_button_is_enabled() {
        window.button("sourceBrowseButton").requireEnabled();
    }

    @Test
    void initial_output_folder_browse_button_is_enabled() {
        window.button("outputFolderBrowseButton").requireEnabled();
    }

    @Test
    void initial_encrypt_checkbox_is_unchecked() {
        window.checkBox("encryptCheckBox").requireNotSelected();
    }

    @Test
    void initial_auto_rotate_checkbox_is_unchecked() {
        window.checkBox("autoRotateCheckBox").requireNotSelected();
    }

    @Test
    void initial_password_fields_are_disabled() {
        window.textBox("ownerPasswordField").requireDisabled();
        window.textBox("userPasswordField").requireDisabled();
    }

    @Test
    void initial_direction_combo_is_enabled() {
        window.comboBox("directionComboBox").requireEnabled();
    }

    @Test
    void initial_align_combos_are_enabled() {
        window.comboBox("horizontalAlignComboBox").requireEnabled();
        window.comboBox("verticalAlignComboBox").requireEnabled();
    }

    // ===== B. Running State 完整覆蓋 =====

    @Test
    void set_running_true_disables_clear_all_button() {
        GuiActionRunner.execute(() -> mediator.setRunningState(true));
        window.button("clearAllButton").requireDisabled();
    }

    @Test
    void set_running_true_disables_source_browse_button() {
        GuiActionRunner.execute(() -> mediator.setRunningState(true));
        window.button("sourceBrowseButton").requireDisabled();
    }

    @Test
    void set_running_true_disables_output_folder_browse_button() {
        GuiActionRunner.execute(() -> mediator.setRunningState(true));
        window.button("outputFolderBrowseButton").requireDisabled();
    }

    @Test
    void set_running_false_restores_all_navigation_buttons() {
        GuiActionRunner.execute(() -> mediator.setRunningState(true));
        GuiActionRunner.execute(() -> mediator.setRunningState(false));
        window.button("convertButton").requireEnabled();
        window.button("clearAllButton").requireEnabled();
        window.button("sourceBrowseButton").requireEnabled();
        window.button("outputFolderBrowseButton").requireEnabled();
        window.button("stopButton").requireDisabled();
    }

    // ===== C. Running State 隔離性 =====

    @Test
    void running_state_does_not_affect_password_fields_when_encryption_off() {
        GuiActionRunner.execute(() -> mediator.setRunningState(true));
        window.textBox("ownerPasswordField").requireDisabled();
        window.textBox("userPasswordField").requireDisabled();
    }

    @Test
    void running_state_does_not_affect_password_fields_when_encryption_on() {
        GuiActionRunner.execute(() -> mediator.notifyUI("encryption_change", true));
        GuiActionRunner.execute(() -> mediator.setRunningState(true));
        window.textBox("ownerPasswordField").requireEnabled();
        window.textBox("userPasswordField").requireEnabled();
    }

    @Test
    void running_state_does_not_affect_align_combos_when_page_is_a4() {
        GuiActionRunner.execute(() -> mediator.setRunningState(true));
        window.comboBox("horizontalAlignComboBox").requireEnabled();
        window.comboBox("verticalAlignComboBox").requireEnabled();
    }

    @Test
    void set_running_false_does_not_restore_align_combos_disabled_by_depend_on_img() {
        GuiActionRunner.execute(() -> mediator.notifyUI("page_size_change", PageSize.DEPEND_ON_IMG));
        GuiActionRunner.execute(() -> mediator.setRunningState(true));
        window.comboBox("horizontalAlignComboBox").requireDisabled();
        window.comboBox("verticalAlignComboBox").requireDisabled();
        // 停止後也不應恢復（page size 仍是 DEPEND_ON_IMG）
        GuiActionRunner.execute(() -> mediator.setRunningState(false));
        window.comboBox("horizontalAlignComboBox").requireDisabled();
        window.comboBox("verticalAlignComboBox").requireDisabled();
    }

    @Test
    void running_state_does_not_affect_direction_combo_when_auto_rotate_off() {
        GuiActionRunner.execute(() -> mediator.setRunningState(true));
        window.comboBox("directionComboBox").requireEnabled();
        GuiActionRunner.execute(() -> mediator.setRunningState(false));
        window.comboBox("directionComboBox").requireEnabled();
    }

    @Test
    void set_running_false_does_not_restore_direction_combo_disabled_by_auto_rotate() {
        GuiActionRunner.execute(() -> mediator.notifyUI("auto_rotate_change", true));
        GuiActionRunner.execute(() -> mediator.setRunningState(true));
        window.comboBox("directionComboBox").requireDisabled();
        GuiActionRunner.execute(() -> mediator.setRunningState(false));
        window.comboBox("directionComboBox").requireDisabled();
    }

    // ===== D. 加密邊界條件 =====

    @Test
    void password_change_event_ignored_when_encryption_off() {
        GuiActionRunner.execute(() -> mediator.notifyUI("owner_password_change", "secret"));
        assertThat(UIState.getInstance().getOwnerPassword()).isNullOrEmpty();
    }

    @Test
    void password_change_event_updates_uistate_when_encryption_on() {
        GuiActionRunner.execute(() -> mediator.notifyUI("encryption_change", true));
        GuiActionRunner.execute(() -> mediator.notifyUI("owner_password_change", "secret"));
        assertThat(UIState.getInstance().getOwnerPassword()).isEqualTo("secret");
    }

    @Test
    void disabling_encryption_clears_uistate_passwords() {
        GuiActionRunner.execute(() -> mediator.notifyUI("encryption_change", true));
        GuiActionRunner.execute(() -> mediator.notifyUI("owner_password_change", "secret"));
        GuiActionRunner.execute(() -> mediator.notifyUI("user_password_change", "pass"));
        GuiActionRunner.execute(() -> mediator.notifyUI("encryption_change", false));
        assertThat(UIState.getInstance().getOwnerPassword()).isEmpty();
        assertThat(UIState.getInstance().getUserPassword()).isEmpty();
    }

    @Test
    void re_enabling_encryption_after_disable_leaves_fields_empty() {
        GuiActionRunner.execute(() -> mediator.notifyUI("encryption_change", true));
        window.textBox("ownerPasswordField").enterText("secret");
        GuiActionRunner.execute(() -> mediator.notifyUI("encryption_change", false));
        GuiActionRunner.execute(() -> mediator.notifyUI("encryption_change", true));
        window.textBox("ownerPasswordField").requireEmpty();
        window.textBox("userPasswordField").requireEmpty();
    }

    @Test
    void rapid_encrypt_on_off_leaves_fields_disabled() {
        GuiActionRunner.execute(() -> {
            mediator.notifyUI("encryption_change", true);
            mediator.notifyUI("encryption_change", false);
        });
        window.textBox("ownerPasswordField").requireDisabled();
        window.textBox("userPasswordField").requireDisabled();
    }

    @Test
    void rapid_encrypt_on_off_on_leaves_fields_enabled() {
        GuiActionRunner.execute(() -> {
            mediator.notifyUI("encryption_change", true);
            mediator.notifyUI("encryption_change", false);
            mediator.notifyUI("encryption_change", true);
        });
        window.textBox("ownerPasswordField").requireEnabled();
        window.textBox("userPasswordField").requireEnabled();
    }

    @Test
    void encrypt_change_updates_uistate_encrypted_flag() {
        GuiActionRunner.execute(() -> mediator.notifyUI("encryption_change", true));
        assertThat(UIState.getInstance().isEncrypted()).isTrue();
        GuiActionRunner.execute(() -> mediator.notifyUI("encryption_change", false));
        assertThat(UIState.getInstance().isEncrypted()).isFalse();
    }

    // ===== E. AutoRotate 邊界條件 =====

    @Test
    void auto_rotate_on_forces_portrait_in_uistate() {
        // 先設 Landscape，再啟用 autoRotate，UIState 應被強制回 Portrait
        GuiActionRunner.execute(() -> mediator.notifyUI("page_direction_change", PageDirection.Landscape));
        GuiActionRunner.execute(() -> mediator.notifyUI("auto_rotate_change", true));
        assertThat(UIState.getInstance().getPageDirection()).isEqualTo(PageDirection.Portrait);
    }

    @Test
    void rapid_auto_rotate_on_off_leaves_direction_combo_enabled() {
        GuiActionRunner.execute(() -> {
            mediator.notifyUI("auto_rotate_change", true);
            mediator.notifyUI("auto_rotate_change", false);
        });
        window.comboBox("directionComboBox").requireEnabled();
    }

    @Test
    void auto_rotate_on_updates_uistate_flag() {
        GuiActionRunner.execute(() -> mediator.notifyUI("auto_rotate_change", true));
        assertThat(UIState.getInstance().isAutoRotate()).isTrue();
        GuiActionRunner.execute(() -> mediator.notifyUI("auto_rotate_change", false));
        assertThat(UIState.getInstance().isAutoRotate()).isFalse();
    }

    // ===== F. PageSize 邊界條件 =====

    @Test
    void switching_from_depend_on_img_to_a4_re_enables_align_combos() {
        GuiActionRunner.execute(() -> mediator.notifyUI("page_size_change", PageSize.DEPEND_ON_IMG));
        GuiActionRunner.execute(() -> mediator.notifyUI("page_size_change", PageSize.A4));
        window.comboBox("horizontalAlignComboBox").requireEnabled();
        window.comboBox("verticalAlignComboBox").requireEnabled();
    }

    @Test
    void depend_on_img_resets_align_combos_display_to_center() {
        // 先在 combo 上選非 CENTER（透過 EDT 直接設值，這樣 comboBoxChanged 事件會觸發）
        GuiActionRunner.execute(() -> {
            window.comboBox("horizontalAlignComboBox").target()
                    .setSelectedItem(PageAlign.HorizontalAlign.LEFT);
            window.comboBox("verticalAlignComboBox").target()
                    .setSelectedItem(PageAlign.VerticalAlign.TOP);
        });
        GuiActionRunner.execute(() -> mediator.notifyUI("page_size_change", PageSize.DEPEND_ON_IMG));
        assertThat(window.comboBox("horizontalAlignComboBox").target().getSelectedItem())
                .isEqualTo(PageAlign.HorizontalAlign.CENTER);
        assertThat(window.comboBox("verticalAlignComboBox").target().getSelectedItem())
                .isEqualTo(PageAlign.VerticalAlign.CENTER);
    }

    @Test
    void page_size_change_updates_uistate() {
        GuiActionRunner.execute(() -> mediator.notifyUI("page_size_change", PageSize.A3));
        assertThat(UIState.getInstance().getPageSize()).isEqualTo(PageSize.A3);
    }

    // ===== G. UIState 更新驗證 =====

    @Test
    void horizontal_align_change_updates_uistate() {
        GuiActionRunner.execute(() ->
                mediator.notifyUI("horizontal_align_change", PageAlign.HorizontalAlign.LEFT));
        assertThat(UIState.getInstance().getHorizontalAlign())
                .isEqualTo(PageAlign.HorizontalAlign.LEFT);
    }

    @Test
    void vertical_align_change_updates_uistate() {
        GuiActionRunner.execute(() ->
                mediator.notifyUI("vertical_align_change", PageAlign.VerticalAlign.TOP));
        assertThat(UIState.getInstance().getVerticalAlign())
                .isEqualTo(PageAlign.VerticalAlign.TOP);
    }

    @Test
    void page_direction_change_updates_uistate() {
        GuiActionRunner.execute(() ->
                mediator.notifyUI("page_direction_change", PageDirection.Landscape));
        assertThat(UIState.getInstance().getPageDirection())
                .isEqualTo(PageDirection.Landscape);
    }

    @Test
    void color_type_change_updates_uistate() {
        // 取 colorTypeComboBox 中第一個非預設值（避免硬編碼 enum 常數名稱）
        ColorType nonDefault = GuiActionRunner.execute(() -> {
            JComboBox<?> cb = window.comboBox("colorTypeComboBox").target();
            for (int i = 0; i < cb.getItemCount(); i++) {
                ColorType item = (ColorType) cb.getItemAt(i);
                if (item != ColorType.sRGB) return item;
            }
            return (ColorType) cb.getItemAt(0);
        });
        GuiActionRunner.execute(() -> mediator.notifyUI("color_type_change", nonDefault));
        assertThat(UIState.getInstance().getColorType()).isEqualTo(nonDefault);
    }

    // ===== H. Progress Bar 與 Label =====

    @Test
    void set_batch_progress_updates_progress_bar_value() {
        GuiActionRunner.execute(() -> mediator.setBatchProgress(3, 10));
        assertThat(window.progressBar("totalConversionProgressBar").target().getValue())
                .isEqualTo(3);
    }

    @Test
    void set_batch_progress_updates_label_text() {
        GuiActionRunner.execute(() -> mediator.setBatchProgress(3, 10));
        assertThat(window.label("totalConversionLabel").text()).isEqualTo("3/10");
    }

    @Test
    void set_conversion_progress_updates_progress_bar_value() {
        GuiActionRunner.execute(() -> mediator.setConversionProgress(5, 20));
        assertThat(window.progressBar("pageConversionProgressBar").target().getValue())
                .isEqualTo(5);
    }

    @Test
    void set_conversion_progress_updates_label_text() {
        GuiActionRunner.execute(() -> mediator.setConversionProgress(5, 20));
        assertThat(window.label("pageConversionLabel").text()).isEqualTo("5/20");
    }

    // ===== I. Log 操作 =====

    @Test
    void add_log_appends_to_log_list() {
        GuiActionRunner.execute(() -> mediator.addLog("line1"));
        assertThat(window.list("logList").target().getModel().getSize()).isEqualTo(1);
    }

    @Test
    void clear_log_empties_log_list() {
        GuiActionRunner.execute(() -> {
            mediator.addLog("line1");
            mediator.addLog("line2");
            mediator.clearLog();
        });
        assertThat(window.list("logList").target().getModel().getSize()).isEqualTo(0);
    }

    @Test
    void multiple_add_log_accumulates_entries() {
        GuiActionRunner.execute(() -> {
            mediator.addLog("a");
            mediator.addLog("b");
            mediator.addLog("c");
        });
        assertThat(window.list("logList").target().getModel().getSize()).isEqualTo(3);
    }

    // ===== J. Convert 按鈕 enable/disable 黑箱測試 =====

    @Test
    void convert_disabled_with_no_tasks() {
        // 初始狀態：無任何 task，Convert 應 DISABLED
        window.button("convertButton").requireDisabled();
    }

    @Test
    void convert_enabled_when_tasks_present_no_encrypt() {
        // 有 task、無加密，Convert 應 ENABLED
        GuiActionRunner.execute(() -> mediator.updateTasks(Arrays.asList(createDummyTask())));
        window.button("convertButton").requireEnabled();
    }

    @Test
    void convert_disabled_after_all_tasks_cleared() {
        // 加入 task 後清空，Convert 應 DISABLED
        GuiActionRunner.execute(() -> mediator.updateTasks(Arrays.asList(createDummyTask())));
        GuiActionRunner.execute(() -> mediator.updateTasks(Collections.emptyList()));
        window.button("convertButton").requireDisabled();
    }

    @Test
    void convert_disabled_when_encrypt_on_no_password() {
        // 有 task + 開啟加密 + 兩個密碼均未填，Convert 應 DISABLED
        GuiActionRunner.execute(() -> mediator.updateTasks(Arrays.asList(createDummyTask())));
        GuiActionRunner.execute(() -> mediator.notifyUI("encryption_change", true));
        window.button("convertButton").requireDisabled();
    }

    @Test
    void convert_disabled_when_encrypt_on_owner_only() {
        // 有 task + 開啟加密 + 只填 ownerPassword，Convert 應 DISABLED
        GuiActionRunner.execute(() -> mediator.updateTasks(Arrays.asList(createDummyTask())));
        GuiActionRunner.execute(() -> mediator.notifyUI("encryption_change", true));
        window.textBox("ownerPasswordField").enterText("owner123");
        window.button("convertButton").requireDisabled();
    }

    @Test
    void convert_disabled_when_encrypt_on_user_only() {
        // 有 task + 開啟加密 + 只填 userPassword，Convert 應 DISABLED
        GuiActionRunner.execute(() -> mediator.updateTasks(Arrays.asList(createDummyTask())));
        GuiActionRunner.execute(() -> mediator.notifyUI("encryption_change", true));
        window.textBox("userPasswordField").enterText("user123");
        window.button("convertButton").requireDisabled();
    }

    @Test
    void convert_enabled_when_encrypt_on_both_passwords() {
        // 有 task + 開啟加密 + 兩個密碼均填，Convert 應 ENABLED
        GuiActionRunner.execute(() -> mediator.updateTasks(Arrays.asList(createDummyTask())));
        GuiActionRunner.execute(() -> mediator.notifyUI("encryption_change", true));
        window.textBox("ownerPasswordField").enterText("owner123");
        window.textBox("userPasswordField").enterText("user123");
        window.button("convertButton").requireEnabled();
    }

    @Test
    void convert_disabled_when_owner_password_cleared() {
        // 有 task + 開啟加密 + 填兩個密碼後清空 ownerPassword，Convert 應 DISABLED
        GuiActionRunner.execute(() -> mediator.updateTasks(Arrays.asList(createDummyTask())));
        GuiActionRunner.execute(() -> mediator.notifyUI("encryption_change", true));
        window.textBox("ownerPasswordField").enterText("owner123");
        window.textBox("userPasswordField").enterText("user123");
        window.textBox("ownerPasswordField").deleteText();
        window.button("convertButton").requireDisabled();
    }

    @Test
    void convert_enabled_after_encrypt_disabled_with_tasks() {
        // 有 task + 開啟加密後再關閉，Convert 應回歸「有 task → ENABLED」
        GuiActionRunner.execute(() -> mediator.updateTasks(Arrays.asList(createDummyTask())));
        GuiActionRunner.execute(() -> mediator.notifyUI("encryption_change", true));
        GuiActionRunner.execute(() -> mediator.notifyUI("encryption_change", false));
        window.button("convertButton").requireEnabled();
    }

    // ===== K. 有 Task 時 Running State 按鈕行為 =====

    @Test
    void hasTask_beforeRunning_convertEnabled() {
        // 有 Task，尚未執行 → Convert ENABLED
        GuiActionRunner.execute(() -> mediator.updateTasks(Arrays.asList(createDummyTask())));
        window.button("convertButton").requireEnabled();
    }

    @Test
    void hasTask_beforeRunning_stopDisabled() {
        // 有 Task，尚未執行 → Stop DISABLED
        GuiActionRunner.execute(() -> mediator.updateTasks(Arrays.asList(createDummyTask())));
        window.button("stopButton").requireDisabled();
    }

    @Test
    void hasTask_setRunningTrue_convertDisabled_stopEnabled() {
        // 有 Task → setRunningState(true) → Convert DISABLED，Stop ENABLED
        GuiActionRunner.execute(() -> mediator.updateTasks(Arrays.asList(createDummyTask())));
        GuiActionRunner.execute(() -> mediator.setRunningState(true));
        window.button("convertButton").requireDisabled();
        window.button("stopButton").requireEnabled();
    }

    @Test
    void hasTask_setRunningFalse_convertEnabled_stopDisabled() {
        // 有 Task → running → setRunningState(false) → Convert ENABLED，Stop DISABLED
        GuiActionRunner.execute(() -> mediator.updateTasks(Arrays.asList(createDummyTask())));
        GuiActionRunner.execute(() -> mediator.setRunningState(true));
        GuiActionRunner.execute(() -> mediator.setRunningState(false));
        window.button("convertButton").requireEnabled();
        window.button("stopButton").requireDisabled();
    }

    // ===== 輔助方法 =====

    private Task createDummyTask() {
        return new Task(new File("dummy.pdf"), new File[0]);
    }
}
