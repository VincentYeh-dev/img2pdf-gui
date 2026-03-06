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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.vincentyeh.img2pdf.gui.model.TaskSortOrder;

import javax.swing.tree.DefaultMutableTreeNode;

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

    // Verifies that checking the encrypt checkbox enables both password fields.
    @Test
    void check_encrypt_enables_password_fields() {
        window.checkBox("encryptCheckBox").check();
        window.textBox("ownerPasswordField").requireEnabled();
        window.textBox("userPasswordField").requireEnabled();
    }

    // Verifies that disabling encryption disables the password fields and clears any entered text.
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

    // Verifies that checking the auto-rotate checkbox disables the direction combo box.
    @Test
    void check_autoRotate_disables_direction_combo() {
        window.checkBox("autoRotateCheckBox").check();
        window.comboBox("directionComboBox").requireDisabled();
    }

    // Verifies that unchecking auto-rotate re-enables the direction combo box.
    @Test
    void uncheck_autoRotate_enables_direction_combo() {
        GuiActionRunner.execute(() -> mediator.notifyUI("auto_rotate_change", true));
        window.comboBox("directionComboBox").requireDisabled();

        GuiActionRunner.execute(() -> mediator.notifyUI("auto_rotate_change", false));
        window.comboBox("directionComboBox").requireEnabled();
    }

    // Verifies that selecting DEPEND_ON_IMG page size disables both alignment combo boxes.
    @Test
    void depend_on_img_page_size_disables_align_combos() {
        GuiActionRunner.execute(() ->
                mediator.notifyUI("page_size_change", PageSize.DEPEND_ON_IMG));
        window.comboBox("horizontalAlignComboBox").requireDisabled();
        window.comboBox("verticalAlignComboBox").requireDisabled();
    }

    // Verifies that entering running state disables the convert button and enables the stop button.
    @Test
    void set_running_true_disables_convert_and_enables_stop() {
        GuiActionRunner.execute(() -> mediator.setRunningState(true));
        window.button("convertButton").requireDisabled();
        window.button("stopButton").requireEnabled();
    }

    // Verifies that leaving running state re-enables the convert button (when tasks exist)
    // and disables the stop button.
    @Test
    void set_running_false_enables_convert_and_disables_stop() {
        GuiActionRunner.execute(() -> mediator.updateTasks(Collections.singletonList(new TaskDisplay("a.pdf", new File[0]))));
        GuiActionRunner.execute(() -> mediator.setRunningState(false));
        window.button("convertButton").requireEnabled();
        window.button("stopButton").requireDisabled();
    }

    // Verifies that the output folder text field is read-only and cannot be typed into directly.
    @Test
    void output_folder_field_is_not_editable() {
        window.textBox("outputFolderField").requireNotEditable();
    }

    // Verifies that the output folder field shows an absolute path on startup, not a bare ".".
    @Test
    void output_folder_initial_value_is_absolute_path() {
        String text = window.textBox("outputFolderField").text();
        assertThat(text).isNotEqualTo(".");
        assertThat(new File(text).isAbsolute()).isTrue();
    }

    // Verifies that an output_folder_change event updates the destination folder in UIState.
    @Test
    void output_folder_change_event_updates_uistate() {
        GuiActionRunner.execute(() -> mediator.notifyUI("output_folder_change", "output"));
        assertThat(UIState.getInstance().getDestinationFolder())
                .isEqualTo(new File("output"));
    }

    // ===== A. 初始狀態 =====

    // Verifies that the convert button is disabled on startup when no tasks are loaded.
    @Test
    void initial_convert_button_is_disabled_without_tasks() {
        window.button("convertButton").requireDisabled();
    }

    // Verifies that the stop button is disabled on startup.
    @Test
    void initial_stop_button_is_disabled() {
        window.button("stopButton").requireDisabled();
    }

    // Verifies that the clear-all button is enabled on startup.
    @Test
    void initial_clear_all_button_is_enabled() {
        window.button("clearAllButton").requireEnabled();
    }

    // Verifies that the source browse button is enabled on startup.
    @Test
    void initial_source_browse_button_is_enabled() {
        window.button("sourceBrowseButton").requireEnabled();
    }

    // Verifies that the output folder browse button is enabled on startup.
    @Test
    void initial_output_folder_browse_button_is_enabled() {
        window.button("outputFolderBrowseButton").requireEnabled();
    }

    // Verifies that the encrypt checkbox is unchecked on startup.
    @Test
    void initial_encrypt_checkbox_is_unchecked() {
        window.checkBox("encryptCheckBox").requireNotSelected();
    }

    // Verifies that the auto-rotate checkbox is unchecked on startup.
    @Test
    void initial_auto_rotate_checkbox_is_unchecked() {
        window.checkBox("autoRotateCheckBox").requireNotSelected();
    }

    // Verifies that both password fields are disabled on startup (encryption is off by default).
    @Test
    void initial_password_fields_are_disabled() {
        window.textBox("ownerPasswordField").requireDisabled();
        window.textBox("userPasswordField").requireDisabled();
    }

    // Verifies that the direction combo box is enabled on startup (auto-rotate is off by default).
    @Test
    void initial_direction_combo_is_enabled() {
        window.comboBox("directionComboBox").requireEnabled();
    }

    // Verifies that both alignment combo boxes are enabled on startup (page size is not DEPEND_ON_IMG by default).
    @Test
    void initial_align_combos_are_enabled() {
        window.comboBox("horizontalAlignComboBox").requireEnabled();
        window.comboBox("verticalAlignComboBox").requireEnabled();
    }

    // ===== B. Running State 完整覆蓋 =====

    // Verifies that entering running state disables the clear-all button.
    @Test
    void set_running_true_disables_clear_all_button() {
        GuiActionRunner.execute(() -> mediator.setRunningState(true));
        window.button("clearAllButton").requireDisabled();
    }

    // Verifies that entering running state disables the source browse button.
    @Test
    void set_running_true_disables_source_browse_button() {
        GuiActionRunner.execute(() -> mediator.setRunningState(true));
        window.button("sourceBrowseButton").requireDisabled();
    }

    // Verifies that entering running state disables the output folder browse button.
    @Test
    void set_running_true_disables_output_folder_browse_button() {
        GuiActionRunner.execute(() -> mediator.setRunningState(true));
        window.button("outputFolderBrowseButton").requireDisabled();
    }

    // Verifies that leaving running state restores all navigation buttons to their enabled state.
    @Test
    void set_running_false_restores_all_navigation_buttons() {
        GuiActionRunner.execute(() -> mediator.updateTasks(Collections.singletonList(new TaskDisplay("a.pdf", new File[0]))));
        GuiActionRunner.execute(() -> mediator.setRunningState(true));
        GuiActionRunner.execute(() -> mediator.setRunningState(false));
        window.button("convertButton").requireEnabled();
        window.button("clearAllButton").requireEnabled();
        window.button("sourceBrowseButton").requireEnabled();
        window.button("outputFolderBrowseButton").requireEnabled();
        window.button("stopButton").requireDisabled();
    }

    // ===== C. Running State 隔離性 =====

    // Verifies that entering running state does not affect password fields when encryption is disabled.
    @Test
    void running_state_does_not_affect_password_fields_when_encryption_off() {
        GuiActionRunner.execute(() -> mediator.setRunningState(true));
        window.textBox("ownerPasswordField").requireDisabled();
        window.textBox("userPasswordField").requireDisabled();
    }

    // Verifies that entering running state does not disable password fields when encryption is enabled.
    @Test
    void running_state_does_not_affect_password_fields_when_encryption_on() {
        GuiActionRunner.execute(() -> mediator.notifyUI("encryption_change", true));
        GuiActionRunner.execute(() -> mediator.setRunningState(true));
        window.textBox("ownerPasswordField").requireEnabled();
        window.textBox("userPasswordField").requireEnabled();
    }

    // Verifies that entering running state does not disable alignment combos when page size is not DEPEND_ON_IMG.
    @Test
    void running_state_does_not_affect_align_combos_when_page_is_a4() {
        GuiActionRunner.execute(() -> mediator.setRunningState(true));
        window.comboBox("horizontalAlignComboBox").requireEnabled();
        window.comboBox("verticalAlignComboBox").requireEnabled();
    }

    // Verifies that leaving running state does not re-enable alignment combos that were disabled by DEPEND_ON_IMG.
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

    // Verifies that running state does not affect the direction combo box when auto-rotate is off.
    @Test
    void running_state_does_not_affect_direction_combo_when_auto_rotate_off() {
        GuiActionRunner.execute(() -> mediator.setRunningState(true));
        window.comboBox("directionComboBox").requireEnabled();
        GuiActionRunner.execute(() -> mediator.setRunningState(false));
        window.comboBox("directionComboBox").requireEnabled();
    }

    // Verifies that leaving running state does not re-enable the direction combo that was disabled by auto-rotate.
    @Test
    void set_running_false_does_not_restore_direction_combo_disabled_by_auto_rotate() {
        GuiActionRunner.execute(() -> mediator.notifyUI("auto_rotate_change", true));
        GuiActionRunner.execute(() -> mediator.setRunningState(true));
        window.comboBox("directionComboBox").requireDisabled();
        GuiActionRunner.execute(() -> mediator.setRunningState(false));
        window.comboBox("directionComboBox").requireDisabled();
    }

    // ===== D. 加密邊界條件 =====

    // Verifies that password change events are ignored when encryption is disabled.
    @Test
    void password_change_event_ignored_when_encryption_off() {
        GuiActionRunner.execute(() -> mediator.notifyUI("owner_password_change", "secret"));
        assertThat(UIState.getInstance().getOwnerPassword()).isNullOrEmpty();
    }

    // Verifies that password change events update UIState when encryption is enabled.
    @Test
    void password_change_event_updates_uistate_when_encryption_on() {
        GuiActionRunner.execute(() -> mediator.notifyUI("encryption_change", true));
        GuiActionRunner.execute(() -> mediator.notifyUI("owner_password_change", "secret"));
        assertThat(UIState.getInstance().getOwnerPassword()).isEqualTo("secret");
    }

    // Verifies that disabling encryption clears both passwords from UIState.
    @Test
    void disabling_encryption_clears_uistate_passwords() {
        GuiActionRunner.execute(() -> mediator.notifyUI("encryption_change", true));
        GuiActionRunner.execute(() -> mediator.notifyUI("owner_password_change", "secret"));
        GuiActionRunner.execute(() -> mediator.notifyUI("user_password_change", "pass"));
        GuiActionRunner.execute(() -> mediator.notifyUI("encryption_change", false));
        assertThat(UIState.getInstance().getOwnerPassword()).isEmpty();
        assertThat(UIState.getInstance().getUserPassword()).isEmpty();
    }

    // Verifies that re-enabling encryption after a disable cycle leaves the password fields empty.
    @Test
    void re_enabling_encryption_after_disable_leaves_fields_empty() {
        GuiActionRunner.execute(() -> mediator.notifyUI("encryption_change", true));
        window.textBox("ownerPasswordField").enterText("secret");
        GuiActionRunner.execute(() -> mediator.notifyUI("encryption_change", false));
        GuiActionRunner.execute(() -> mediator.notifyUI("encryption_change", true));
        window.textBox("ownerPasswordField").requireEmpty();
        window.textBox("userPasswordField").requireEmpty();
    }

    // Verifies that rapid on-off encryption toggling leaves the password fields disabled.
    @Test
    void rapid_encrypt_on_off_leaves_fields_disabled() {
        GuiActionRunner.execute(() -> {
            mediator.notifyUI("encryption_change", true);
            mediator.notifyUI("encryption_change", false);
        });
        window.textBox("ownerPasswordField").requireDisabled();
        window.textBox("userPasswordField").requireDisabled();
    }

    // Verifies that rapid on-off-on encryption toggling leaves the password fields enabled.
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

    // Verifies that encryption change events correctly update the encrypted flag in UIState.
    @Test
    void encrypt_change_updates_uistate_encrypted_flag() {
        GuiActionRunner.execute(() -> mediator.notifyUI("encryption_change", true));
        assertThat(UIState.getInstance().isEncrypted()).isTrue();
        GuiActionRunner.execute(() -> mediator.notifyUI("encryption_change", false));
        assertThat(UIState.getInstance().isEncrypted()).isFalse();
    }

    // ===== E. AutoRotate 邊界條件 =====

    // Verifies that enabling auto-rotate forces the UIState page direction to Portrait regardless of prior selection.
    @Test
    void auto_rotate_on_forces_portrait_in_uistate() {
        // 先設 Landscape，再啟用 autoRotate，UIState 應被強制回 Portrait
        GuiActionRunner.execute(() -> mediator.notifyUI("page_direction_change", PageDirection.Landscape));
        GuiActionRunner.execute(() -> mediator.notifyUI("auto_rotate_change", true));
        assertThat(UIState.getInstance().getPageDirection()).isEqualTo(PageDirection.Portrait);
    }

    // Verifies that rapid auto-rotate on-off toggling leaves the direction combo box enabled.
    @Test
    void rapid_auto_rotate_on_off_leaves_direction_combo_enabled() {
        GuiActionRunner.execute(() -> {
            mediator.notifyUI("auto_rotate_change", true);
            mediator.notifyUI("auto_rotate_change", false);
        });
        window.comboBox("directionComboBox").requireEnabled();
    }

    // Verifies that auto-rotate change events correctly update the autoRotate flag in UIState.
    @Test
    void auto_rotate_on_updates_uistate_flag() {
        GuiActionRunner.execute(() -> mediator.notifyUI("auto_rotate_change", true));
        assertThat(UIState.getInstance().isAutoRotate()).isTrue();
        GuiActionRunner.execute(() -> mediator.notifyUI("auto_rotate_change", false));
        assertThat(UIState.getInstance().isAutoRotate()).isFalse();
    }

    // ===== F. PageSize 邊界條件 =====

    // Verifies that switching page size from DEPEND_ON_IMG back to A4 re-enables both alignment combos.
    @Test
    void switching_from_depend_on_img_to_a4_re_enables_align_combos() {
        GuiActionRunner.execute(() -> mediator.notifyUI("page_size_change", PageSize.DEPEND_ON_IMG));
        GuiActionRunner.execute(() -> mediator.notifyUI("page_size_change", PageSize.A4));
        window.comboBox("horizontalAlignComboBox").requireEnabled();
        window.comboBox("verticalAlignComboBox").requireEnabled();
    }

    // Verifies that switching to DEPEND_ON_IMG resets both alignment combos to CENTER.
    // [White-box] Directly invokes setSelectedItem() on the combo box target to set a non-CENTER initial value.
    // Justification: AssertJ Swing has no public API to programmatically select a specific enum item in a combo box;
    // direct target manipulation is the only reliable way to establish the non-CENTER precondition for this test.
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

    // Verifies that a page_size_change event updates the page size in UIState.
    @Test
    void page_size_change_updates_uistate() {
        GuiActionRunner.execute(() -> mediator.notifyUI("page_size_change", PageSize.A3));
        assertThat(UIState.getInstance().getPageSize()).isEqualTo(PageSize.A3);
    }

    // ===== G. UIState 更新驗證 =====

    // Verifies that a horizontal_align_change event updates the horizontal alignment in UIState.
    @Test
    void horizontal_align_change_updates_uistate() {
        GuiActionRunner.execute(() ->
                mediator.notifyUI("horizontal_align_change", PageAlign.HorizontalAlign.LEFT));
        assertThat(UIState.getInstance().getHorizontalAlign())
                .isEqualTo(PageAlign.HorizontalAlign.LEFT);
    }

    // Verifies that a vertical_align_change event updates the vertical alignment in UIState.
    @Test
    void vertical_align_change_updates_uistate() {
        GuiActionRunner.execute(() ->
                mediator.notifyUI("vertical_align_change", PageAlign.VerticalAlign.TOP));
        assertThat(UIState.getInstance().getVerticalAlign())
                .isEqualTo(PageAlign.VerticalAlign.TOP);
    }

    // Verifies that a page_direction_change event updates the page direction in UIState.
    @Test
    void page_direction_change_updates_uistate() {
        GuiActionRunner.execute(() ->
                mediator.notifyUI("page_direction_change", PageDirection.Landscape));
        assertThat(UIState.getInstance().getPageDirection())
                .isEqualTo(PageDirection.Landscape);
    }

    // Verifies that a color_type_change event updates the color type in UIState.
    // [White-box] Directly accesses the combo box target to enumerate available ColorType items.
    // Justification: The test must select a non-default value without hardcoding enum constants;
    // iterating the combo box model is the only way to discover available items programmatically.
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

    // Verifies that setBatchProgress updates the total conversion progress bar value.
    @Test
    void set_batch_progress_updates_progress_bar_value() {
        GuiActionRunner.execute(() -> mediator.setBatchProgress(3, 10));
        assertThat(window.progressBar("totalConversionProgressBar").target().getValue())
                .isEqualTo(3);
    }

    // Verifies that setBatchProgress updates the total conversion label to show "current/total" format.
    @Test
    void set_batch_progress_updates_label_text() {
        GuiActionRunner.execute(() -> mediator.setBatchProgress(3, 10));
        assertThat(window.label("totalConversionLabel").text()).isEqualTo("3/10");
    }

    // Verifies that setConversionProgress updates the per-page conversion progress bar value.
    @Test
    void set_conversion_progress_updates_progress_bar_value() {
        GuiActionRunner.execute(() -> mediator.setConversionProgress(5, 20));
        assertThat(window.progressBar("pageConversionProgressBar").target().getValue())
                .isEqualTo(5);
    }

    // Verifies that setConversionProgress updates the per-page conversion label to show "current/total" format.
    @Test
    void set_conversion_progress_updates_label_text() {
        GuiActionRunner.execute(() -> mediator.setConversionProgress(5, 20));
        assertThat(window.label("pageConversionLabel").text()).isEqualTo("5/20");
    }

    // ===== J. Convert 按鈕 enable/disable 黑箱測試 =====

    // Verifies that the convert button is disabled when no tasks are present.
    @Test
    void convert_disabled_with_no_tasks() {
        // 初始狀態：無任何 task，Convert 應 DISABLED
        window.button("convertButton").requireDisabled();
    }

    // Verifies that the convert button is enabled when tasks are present and encryption is off.
    @Test
    void convert_enabled_when_tasks_present_no_encrypt() {
        // 有 task、無加密，Convert 應 ENABLED
        GuiActionRunner.execute(() -> mediator.updateTasks(Arrays.asList(createDummyDisplay())));
        window.button("convertButton").requireEnabled();
    }

    // Verifies that the convert button is disabled after all tasks are cleared.
    @Test
    void convert_disabled_after_all_tasks_cleared() {
        // 加入 task 後清空，Convert 應 DISABLED
        GuiActionRunner.execute(() -> mediator.updateTasks(Arrays.asList(createDummyDisplay())));
        GuiActionRunner.execute(() -> mediator.updateTasks(Collections.emptyList()));
        window.button("convertButton").requireDisabled();
    }

    // Verifies that the convert button is disabled when encryption is on but both passwords are empty.
    @Test
    void convert_disabled_when_encrypt_on_no_password() {
        // 有 task + 開啟加密 + 兩個密碼均未填，Convert 應 DISABLED
        GuiActionRunner.execute(() -> mediator.updateTasks(Arrays.asList(createDummyDisplay())));
        GuiActionRunner.execute(() -> mediator.notifyUI("encryption_change", true));
        window.button("convertButton").requireDisabled();
    }

    // Verifies that the convert button is disabled when only the owner password is filled.
    @Test
    void convert_disabled_when_encrypt_on_owner_only() {
        // 有 task + 開啟加密 + 只填 ownerPassword，Convert 應 DISABLED
        GuiActionRunner.execute(() -> mediator.updateTasks(Arrays.asList(createDummyDisplay())));
        GuiActionRunner.execute(() -> mediator.notifyUI("encryption_change", true));
        window.textBox("ownerPasswordField").enterText("owner123");
        window.button("convertButton").requireDisabled();
    }

    // Verifies that the convert button is disabled when only the user password is filled.
    @Test
    void convert_disabled_when_encrypt_on_user_only() {
        // 有 task + 開啟加密 + 只填 userPassword，Convert 應 DISABLED
        GuiActionRunner.execute(() -> mediator.updateTasks(Arrays.asList(createDummyDisplay())));
        GuiActionRunner.execute(() -> mediator.notifyUI("encryption_change", true));
        window.textBox("userPasswordField").enterText("user123");
        window.button("convertButton").requireDisabled();
    }

    // Verifies that the convert button is enabled when tasks exist and both encryption passwords are filled.
    @Test
    void convert_enabled_when_encrypt_on_both_passwords() {
        // 有 task + 開啟加密 + 兩個密碼均填，Convert 應 ENABLED
        GuiActionRunner.execute(() -> mediator.updateTasks(Arrays.asList(createDummyDisplay())));
        GuiActionRunner.execute(() -> mediator.notifyUI("encryption_change", true));
        window.textBox("ownerPasswordField").enterText("owner123");
        window.textBox("userPasswordField").enterText("user123");
        window.button("convertButton").requireEnabled();
    }

    // Verifies that clearing the owner password after both were filled disables the convert button.
    @Test
    void convert_disabled_when_owner_password_cleared() {
        // 有 task + 開啟加密 + 填兩個密碼後清空 ownerPassword，Convert 應 DISABLED
        GuiActionRunner.execute(() -> mediator.updateTasks(Arrays.asList(createDummyDisplay())));
        GuiActionRunner.execute(() -> mediator.notifyUI("encryption_change", true));
        window.textBox("ownerPasswordField").enterText("owner123");
        window.textBox("userPasswordField").enterText("user123");
        window.textBox("ownerPasswordField").deleteText();
        window.button("convertButton").requireDisabled();
    }

    // Verifies that disabling encryption when tasks exist re-enables the convert button.
    @Test
    void convert_enabled_after_encrypt_disabled_with_tasks() {
        // 有 task + 開啟加密後再關閉，Convert 應回歸「有 task → ENABLED」
        GuiActionRunner.execute(() -> mediator.updateTasks(Arrays.asList(createDummyDisplay())));
        GuiActionRunner.execute(() -> mediator.notifyUI("encryption_change", true));
        GuiActionRunner.execute(() -> mediator.notifyUI("encryption_change", false));
        window.button("convertButton").requireEnabled();
    }

    // ===== K. 有 Task 時 Running State 按鈕行為 =====

    // Verifies that the convert button is enabled when tasks are present and not yet running.
    @Test
    void hasTask_beforeRunning_convertEnabled() {
        // 有 Task，尚未執行 → Convert ENABLED
        GuiActionRunner.execute(() -> mediator.updateTasks(Arrays.asList(createDummyDisplay())));
        window.button("convertButton").requireEnabled();
    }

    // Verifies that the stop button is disabled when tasks are present but not yet running.
    @Test
    void hasTask_beforeRunning_stopDisabled() {
        // 有 Task，尚未執行 → Stop DISABLED
        GuiActionRunner.execute(() -> mediator.updateTasks(Arrays.asList(createDummyDisplay())));
        window.button("stopButton").requireDisabled();
    }

    // Verifies that entering running state with tasks disables convert and enables stop.
    @Test
    void hasTask_setRunningTrue_convertDisabled_stopEnabled() {
        // 有 Task → setRunningState(true) → Convert DISABLED，Stop ENABLED
        GuiActionRunner.execute(() -> mediator.updateTasks(Arrays.asList(createDummyDisplay())));
        GuiActionRunner.execute(() -> mediator.setRunningState(true));
        window.button("convertButton").requireDisabled();
        window.button("stopButton").requireEnabled();
    }

    // Verifies that leaving running state with tasks re-enables convert and disables stop.
    @Test
    void hasTask_setRunningFalse_convertEnabled_stopDisabled() {
        // 有 Task → running → setRunningState(false) → Convert ENABLED，Stop DISABLED
        GuiActionRunner.execute(() -> mediator.updateTasks(Arrays.asList(createDummyDisplay())));
        GuiActionRunner.execute(() -> mediator.setRunningState(true));
        GuiActionRunner.execute(() -> mediator.setRunningState(false));
        window.button("convertButton").requireEnabled();
        window.button("stopButton").requireDisabled();
    }

    // ===== L. updateTaskStatus 狀態圖示測試 =====

    // Verifies that updateTaskStatus(task, true) records the task as SUCCESS in the status map.
    // [White-box] Directly accesses JUIMediator.taskStatusMap.
    // Justification: UIMediator interface has no public API to query per-task status;
    // taskStatusMap is the sole data source for JTree icon rendering and cannot be observed externally.
    @Test
    void updateTaskStatus_success_marks_task_as_SUCCESS() {
        TaskDisplay task = createDummyDisplay();
        GuiActionRunner.execute(() -> mediator.updateTasks(Arrays.asList(task)));
        GuiActionRunner.execute(() -> mediator.updateTaskStatus(0, true));
        robot.waitForIdle();
        JUIMediator jMediator = (JUIMediator) mediator;
        assertThat(jMediator.taskStatusMap.get(0))
                .isEqualTo(JUIMediator.TaskStatus.SUCCESS);
    }

    // Verifies that updateTaskStatus(task, false) records the task as FAILED in the status map.
    // [White-box] Directly accesses JUIMediator.taskStatusMap.
    // Justification: UIMediator interface has no public API to query per-task status;
    // taskStatusMap is the sole data source for JTree icon rendering and cannot be observed externally.
    @Test
    void updateTaskStatus_failure_marks_task_as_FAILED() {
        TaskDisplay task = createDummyDisplay();
        GuiActionRunner.execute(() -> mediator.updateTasks(Arrays.asList(task)));
        GuiActionRunner.execute(() -> mediator.updateTaskStatus(0, false));
        robot.waitForIdle();
        JUIMediator jMediator = (JUIMediator) mediator;
        assertThat(jMediator.taskStatusMap.get(0))
                .isEqualTo(JUIMediator.TaskStatus.FAILED);
    }

    // Verifies that calling updateTasks() clears all previously recorded task statuses.
    // [White-box] Directly accesses JUIMediator.taskStatusMap.
    // Justification: UIMediator interface has no public API to query per-task status;
    // taskStatusMap must be empty after updateTasks() to prevent stale icons, but this
    // cannot be observed through any public or FrameFixture API.
    @Test
    void updateTasks_clears_taskStatusMap() {
        TaskDisplay task = createDummyDisplay();
        GuiActionRunner.execute(() -> mediator.updateTasks(Arrays.asList(task)));
        GuiActionRunner.execute(() -> mediator.updateTaskStatus(0, true));
        robot.waitForIdle();
        JUIMediator jMediator = (JUIMediator) mediator;
        assertThat(jMediator.taskStatusMap).isNotEmpty();

        GuiActionRunner.execute(() -> mediator.updateTasks(Collections.emptyList()));
        robot.waitForIdle();
        assertThat(jMediator.taskStatusMap).isEmpty();
    }

    // Verifies that setRunningState(true) clears all previously recorded task statuses.
    // [White-box] Directly accesses JUIMediator.taskStatusMap.
    // Justification: UIMediator interface has no public API to query per-task status;
    // the map must be cleared when a new conversion run starts to reset stale icons,
    // but this cannot be observed through any public or FrameFixture API.
    @Test
    void setRunningTrue_clears_taskStatusMap() {
        TaskDisplay task = createDummyDisplay();
        GuiActionRunner.execute(() -> mediator.updateTasks(Arrays.asList(task)));
        GuiActionRunner.execute(() -> mediator.updateTaskStatus(0, true));
        robot.waitForIdle();
        JUIMediator jMediator = (JUIMediator) mediator;
        assertThat(jMediator.taskStatusMap).isNotEmpty();

        GuiActionRunner.execute(() -> mediator.setRunningState(true));
        robot.waitForIdle();
        assertThat(jMediator.taskStatusMap).isEmpty();
    }

    // ===== M. 排序行為測試 =====

    // Verifies that updateTasks() preserves the given order and displays tasks in the same sequence in the JTree.
    // [White-box] Accesses the JTree's TreeModel root to extract Task objects from DefaultMutableTreeNode children.
    // Justification: JTree has no public API to retrieve the ordered list of user objects;
    // verifying display order requires inspecting the underlying TreeModel structure directly.
    @Test
    void M1_default_sort_NAME_ASC_updateTasks_displays_in_given_order() {
        // Model 已按 NAME_ASC 排序後傳給 updateTasks，JTree 應照順序顯示
        TaskDisplay taskA = new TaskDisplay("a.pdf", new File[0]);
        TaskDisplay taskB = new TaskDisplay("b.pdf", new File[0]);
        TaskDisplay taskC = new TaskDisplay("c.pdf", new File[0]);
        GuiActionRunner.execute(() -> mediator.updateTasks(Arrays.asList(taskA, taskB, taskC)));
        robot.waitForIdle();

        List<String> names = GuiActionRunner.execute(() -> {
            JTree tree = window.tree("sourceTree").target();
            DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getModel().getRoot();
            List<String> result = new ArrayList<>();
            for (int i = 0; i < root.getChildCount(); i++) {
                TaskDisplay d = (TaskDisplay) ((DefaultMutableTreeNode) root.getChildAt(i)).getUserObject();
                result.add(d.destinationName);
            }
            return result;
        });
        assertThat(names).containsExactly("a.pdf", "b.pdf", "c.pdf");
    }

    // Verifies that changing the sort combo box to NAME_DESC fires onSortOrderChange with the correct order.
    // [White-box] Casts mediator to JUIMediator to call setListener() and inject a test listener.
    // Justification: UIMediator interface does not define setListener(); injecting a listener to capture
    // the fired event requires access to the concrete JUIMediator implementation.
    @Test
    void M2_sortComboBox_change_to_NAME_DESC_fires_onSortOrderChange() {
        // 設置 mock listener，驗證 onSortOrderChange 被呼叫且傳入正確的 order
        TaskSortOrder[] captured = {null};
        MediatorListener mockListener = new MediatorListener() {
            @Override public void onSourcesUpdate(UIMediator m, UIState s) {}
            @Override public void onConvertButtonClick(UIMediator m, UIState s) {}
            @Override public void onStopButtonClick(UIMediator m) {}
            @Override public void onTaskRemove(UIMediator m, List<Integer> t) {}
            @Override public void onTaskRemoveFromDisk(UIMediator m, List<Integer> t) {}
            @Override public void onSortOrderChange(UIMediator m, TaskSortOrder order) { captured[0] = order; }
        };
        GuiActionRunner.execute(() -> ((JUIMediator) mediator).setListener(mockListener));

        GuiActionRunner.execute(() ->
                window.comboBox("sortComboBox").target().setSelectedItem(TaskSortOrder.NAME_DESC));
        robot.waitForIdle();

        assertThat(captured[0]).isEqualTo(TaskSortOrder.NAME_DESC);
    }

    // Verifies that updateTasks() with pre-sorted COUNT_DESC data displays tasks in descending file-count order.
    // [White-box] Accesses the JTree's TreeModel root to extract Task objects from DefaultMutableTreeNode children.
    // Justification: JTree has no public API to retrieve the ordered list of user objects;
    // verifying display order requires inspecting the underlying TreeModel structure directly.
    @Test
    void M3_updateTasks_with_COUNT_DESC_sorted_data_shows_correct_order() {
        // 模擬 Model 按 COUNT_DESC 排序後傳給 updateTasks，JTree 應保留該順序
        TaskDisplay task3 = new TaskDisplay("c.pdf", new File[]{new File("f1.jpg"), new File("f2.jpg"), new File("f3.jpg")});
        TaskDisplay task2 = new TaskDisplay("b.pdf", new File[]{new File("f1.jpg"), new File("f2.jpg")});
        TaskDisplay task1 = new TaskDisplay("a.pdf", new File[]{new File("f1.jpg")});
        // COUNT_DESC 順序：3 > 2 > 1
        GuiActionRunner.execute(() -> mediator.updateTasks(Arrays.asList(task3, task2, task1)));
        robot.waitForIdle();

        List<Integer> counts = GuiActionRunner.execute(() -> {
            JTree tree = window.tree("sourceTree").target();
            DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getModel().getRoot();
            List<Integer> result = new ArrayList<>();
            for (int i = 0; i < root.getChildCount(); i++) {
                TaskDisplay d = (TaskDisplay) ((DefaultMutableTreeNode) root.getChildAt(i)).getUserObject();
                result.add(d.sourceFiles.length);
            }
            return result;
        });
        assertThat(counts).containsExactly(3, 2, 1);
    }

    // Verifies that a second updateTasks() call replaces the previous task list and preserves the new given order.
    // [White-box] Accesses the JTree's TreeModel root to extract Task objects from DefaultMutableTreeNode children.
    // Justification: JTree has no public API to retrieve the ordered list of user objects;
    // verifying display order requires inspecting the underlying TreeModel structure directly.
    @Test
    void M4_re_updateTasks_preserves_new_given_order() {
        // 第一次 updateTasks
        TaskDisplay taskB = new TaskDisplay("b.pdf", new File[0]);
        TaskDisplay taskA = new TaskDisplay("a.pdf", new File[0]);
        GuiActionRunner.execute(() -> mediator.updateTasks(Arrays.asList(taskB, taskA)));
        robot.waitForIdle();

        // 再次 updateTasks（模擬 Model 按 NAME_DESC 重新排序後傳入）
        TaskDisplay taskZ = new TaskDisplay("z.pdf", new File[0]);
        TaskDisplay taskM = new TaskDisplay("m.pdf", new File[0]);
        GuiActionRunner.execute(() -> mediator.updateTasks(Arrays.asList(taskZ, taskM)));
        robot.waitForIdle();

        List<String> names = GuiActionRunner.execute(() -> {
            JTree tree = window.tree("sourceTree").target();
            DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getModel().getRoot();
            List<String> result = new ArrayList<>();
            for (int i = 0; i < root.getChildCount(); i++) {
                TaskDisplay d = (TaskDisplay) ((DefaultMutableTreeNode) root.getChildAt(i)).getUserObject();
                result.add(d.destinationName);
            }
            return result;
        });
        assertThat(names).containsExactly("z.pdf", "m.pdf");
    }

    // ===== N. StopButton 點擊通知 Listener =====

    // Verifies that clicking the stop button while running fires onStopButtonClick on the listener exactly once.
    // [White-box] Casts mediator to JUIMediator to call setListener() and inject a test listener.
    // Justification: UIMediator interface does not define setListener(); injecting a listener to capture
    // the fired event requires access to the concrete JUIMediator implementation.
    @Test
    void N1_click_stopButton_while_running_notifies_listener_onStopButtonClick() {
        int[] callCount = {0};
        MediatorListener mockListener = new MediatorListener() {
            @Override public void onSourcesUpdate(UIMediator m, UIState s) {}
            @Override public void onConvertButtonClick(UIMediator m, UIState s) {}
            @Override public void onStopButtonClick(UIMediator m) { callCount[0]++; }
            @Override public void onTaskRemove(UIMediator m, List<Integer> t) {}
            @Override public void onTaskRemoveFromDisk(UIMediator m, List<Integer> t) {}
            @Override public void onSortOrderChange(UIMediator m, TaskSortOrder order) {}
        };
        GuiActionRunner.execute(() -> ((JUIMediator) mediator).setListener(mockListener));
        GuiActionRunner.execute(() -> mediator.setRunningState(true));

        window.button("stopButton").click();
        robot.waitForIdle();

        assertThat(callCount[0]).isEqualTo(1);
    }

    // ===== O. setRunningState(true) 重置 Task 狀態圖示 =====

    // Verifies that setRunningState(true) resets all task statuses to PENDING, clearing any previous SUCCESS/FAILED icons.
    // [White-box] Directly accesses JUIMediator.taskStatusMap.
    // Justification: UIMediator interface has no public API to query per-task status;
    // taskStatusMap is the sole data source for JTree icon rendering and cannot be observed externally.
    @Test
    void setRunningState_true_clears_task_status_to_PENDING() {
        // 1. 建立任務並設置 SUCCESS 狀態
        TaskDisplay task = createDummyDisplay();
        GuiActionRunner.execute(() -> mediator.updateTasks(Arrays.asList(task)));
        GuiActionRunner.execute(() -> mediator.updateTaskStatus(0, true));
        robot.waitForIdle();

        JUIMediator jMediator = (JUIMediator) mediator;
        // 確認狀態已是 SUCCESS
        assertThat(jMediator.taskStatusMap.getOrDefault(0, JUIMediator.TaskStatus.PENDING))
                .isEqualTo(JUIMediator.TaskStatus.SUCCESS);

        // 2. setRunningState(true) 後，renderer 應讀取到 PENDING
        GuiActionRunner.execute(() -> mediator.setRunningState(true));
        robot.waitForIdle();

        assertThat(jMediator.taskStatusMap.getOrDefault(0, JUIMediator.TaskStatus.PENDING))
                .isEqualTo(JUIMediator.TaskStatus.PENDING);
    }

    // ===== 輔助方法 =====

    private TaskDisplay createDummyDisplay() {
        return new TaskDisplay("dummy.pdf", new File[0]);
    }
}
