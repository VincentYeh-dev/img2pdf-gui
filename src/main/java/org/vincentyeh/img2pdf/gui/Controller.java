package org.vincentyeh.img2pdf.gui;

import org.vincentyeh.img2pdf.gui.ui.View;
import org.vincentyeh.img2pdf.lib.image.ColorType;
import org.vincentyeh.img2pdf.lib.pdf.parameter.*;

import java.io.File;

public class Controller {
    private final Model model;
    private PageAlign.HorizontalAlign horizontalAlign;
    private PageAlign.VerticalAlign verticalAlign;

    private PageDirection pageDirection;
    private boolean autoRotate;
    private PageSize pageSize;

    private String owner_password, user_password;
    private ColorType colorType;

    public Controller(Model model){
        this.model = model;
    }

    public void setFileFilter(String filter){
        model.setFileFilter(filter);
    }
    public String getFileFilter(){
        return model.getFileFilter();
    }

    public String getOutputFormat() {
        return model.getOutputFormat();
    }

    public void setOutputFormat(String output_format) {
        model.setOutputFormat(output_format);
    }

    public File getOutputFolder() {
        return model.getOutputFolder();
    }

    public void setOutputFolder(File output_folder) {
        model.setOutputFolder(output_folder);
    }

    public void addSource(File[] files){
        model.addSource(files);
    }

    public void setPageDirection(PageDirection pageDirection) {
        this.pageDirection=pageDirection;
    }

    public void setAutoRotate(boolean autoRotate) {
        this.autoRotate=autoRotate;
    }
    public void setPageSize(PageSize pageSize) {
        this.pageSize=pageSize;
    }

    public void setColorType(ColorType colorType) {
        model.setColorType(colorType);
    }

    public void setHorizontalAlign(PageAlign.HorizontalAlign horizontalAlign) {
        this.horizontalAlign = horizontalAlign;
    }

    void setVerticalAlign(PageAlign.VerticalAlign verticalAlign){
        this.verticalAlign = verticalAlign;
    }


    public void setOwnerPassword(String owner_password) {
        this.owner_password = owner_password.length() > 0 ? owner_password : null;
    }

    public void setUserPassword(String user_password) {
        this.user_password = user_password.length() > 0 ? user_password : null;
    }

    public void convert(){
        model.setPageArgument(createPageArgument());
        model.setDocumentArgument(createDocumentArgument());
        model.convert();
    }

    public void clearSource() {
        model.clearSource();
    }


//    public void updateView(){
//        view.setFileFilterField(model.getFileFilter());
//    }

    private PageArgument createPageArgument() {
        return new PageArgument(new PageAlign(verticalAlign,horizontalAlign), pageSize, pageDirection, autoRotate);
    }

    private DocumentArgument createDocumentArgument() {
        return new DocumentArgument(owner_password, user_password);
    }


}
