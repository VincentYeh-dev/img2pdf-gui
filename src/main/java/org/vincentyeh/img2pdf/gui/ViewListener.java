package org.vincentyeh.img2pdf.gui;

import org.vincentyeh.img2pdf.lib.image.ColorType;
import org.vincentyeh.img2pdf.lib.pdf.parameter.PageAlign;
import org.vincentyeh.img2pdf.lib.pdf.parameter.PageDirection;
import org.vincentyeh.img2pdf.lib.pdf.parameter.PageSize;

import java.io.File;

public interface ViewListener {
    void onConvertButtonClicked();
    void onClearAllButtonClicked();

    void onAutoRotateCheckBoxChanged(boolean check);

    void onSizeComboChanged(PageSize pageSize);

    void onStopButtonClicked();

    void onFileFilterFieldChange(String text);

    void onOutputFormatFieldChange(String text);

    void onSourcesFileSelected(File[] selectedFiles);

    void onOutputFolderChanged(File selectedFile);

    void onComboColorChanged(ColorType selectedItem);

    void onComboHorizontalAlignChanged(PageAlign.HorizontalAlign selectedItem);

    void onComboVerticalAlignChanged(PageAlign.VerticalAlign selectedItem);

    void onComboDirectionChanged(PageDirection selectedItem);
}
