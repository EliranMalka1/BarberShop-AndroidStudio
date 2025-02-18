package com.example.navigtion_app.models;

public class ButtonItem {
    private String buttonText;
    private int iconResource;
    private int backgroundColor;
    private int navigationActionId; // שמירת ID של הניווט

    public ButtonItem(String buttonText, int iconResource, int backgroundColor, int navigationActionId) {
        this.buttonText = buttonText;
        this.iconResource = iconResource;
        this.backgroundColor = backgroundColor;
        this.navigationActionId = navigationActionId;
    }

    public String getButtonText() { return buttonText; }
    public int getIconResource() { return iconResource; }
    public int getBackgroundColor() { return backgroundColor; }
    public int getNavigationActionId() { return navigationActionId; }
}
