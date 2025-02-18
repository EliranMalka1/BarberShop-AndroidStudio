package com.example.navigtion_app.models;

import androidx.fragment.app.Fragment;

public class ButtonItem {
    private String buttonText;
    private int iconResource;
    private int backgroundColor;
    private Class<? extends Fragment> targetFragment; // שינוי מ-Activity ל-Fragment

    public ButtonItem(String buttonText, int iconResource, int backgroundColor, Class<? extends Fragment> targetFragment) {
        this.buttonText = buttonText;
        this.iconResource = iconResource;
        this.backgroundColor = backgroundColor;
        this.targetFragment = targetFragment;
    }

    public String getButtonText() { return buttonText; }
    public int getIconResource() { return iconResource; }
    public int getBackgroundColor() { return backgroundColor; }
    public Class<? extends Fragment> getTargetFragment() { return targetFragment; }
}
