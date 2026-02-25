package org.vincentyeh.img2pdf.gui.view;

import org.assertj.swing.core.BasicRobot;
import org.assertj.swing.core.Robot;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.vincentyeh.img2pdf.lib.pdf.parameter.PageSize;

import javax.swing.*;
import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

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
    void typing_output_folder_updates_uistate() {
        // View initializes the field with "." before the DocumentListener is attached,
        // so the initial UIState destination is null; delete existing text then type.
        window.textBox("outputFolderField").deleteText().enterText("output");
        assertThat(UIState.getInstance().getDestinationFolder())
                .isEqualTo(new File("output"));
    }
}
