package com.example;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.ui.analytics.AnalyticsFragment;
import com.example.ui.dashboard.DashboardFragment;
import com.example.ui.network.NetworkFragment;
import com.example.ui.notifications.NotificationsFragment;
import com.example.ui.usage.UsageFragment;

public class MainActivity extends AppCompatActivity {
    private static final int TAB_HOME = 0;
    private static final int TAB_NETWORK = 1;
    private static final int TAB_USAGE = 2;
    private static final int TAB_NOTIFICATIONS = 3;
    private static final int TAB_ANALYTICS = 4;

    private int currentTab = TAB_HOME;

    private LinearLayout navItemHome;
    private LinearLayout navItemNetwork;
    private LinearLayout navItemUsage;
    private LinearLayout navItemNotifications;
    private LinearLayout navItemAnalytics;

    private ImageView navIconHome;
    private ImageView navIconNetwork;
    private ImageView navIconUsage;
    private ImageView navIconNotifications;
    private ImageView navIconAnalytics;

    private TextView navLabelHome;
    private TextView navLabelNetwork;
    private TextView navLabelUsage;
    private TextView navLabelNotifications;
    private TextView navLabelAnalytics;

    private View glassBottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Enable edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_main);

        initViews();
        setupWindowInsets();
        setupNavListeners();

        if (savedInstanceState == null) {
            selectTab(TAB_HOME);
        }
    }

    private void initViews() {
        glassBottomNav = findViewById(R.id.glass_bottom_nav);

        navItemHome = findViewById(R.id.nav_item_home);
        navItemNetwork = findViewById(R.id.nav_item_network);
        navItemUsage = findViewById(R.id.nav_item_usage);
        navItemNotifications = findViewById(R.id.nav_item_notifications);
        navItemAnalytics = findViewById(R.id.nav_item_analytics);

        navIconHome = findViewById(R.id.nav_icon_home);
        navIconNetwork = findViewById(R.id.nav_icon_network);
        navIconUsage = findViewById(R.id.nav_icon_usage);
        navIconNotifications = findViewById(R.id.nav_icon_notifications);
        navIconAnalytics = findViewById(R.id.nav_icon_analytics);

        navLabelHome = findViewById(R.id.nav_label_home);
        navLabelNetwork = findViewById(R.id.nav_label_network);
        navLabelUsage = findViewById(R.id.nav_label_usage);
        navLabelNotifications = findViewById(R.id.nav_label_notifications);
        navLabelAnalytics = findViewById(R.id.nav_label_analytics);
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_root), (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            // Adjust bottom margin of floating capsule to sit cleanly above gesture bar
            if (glassBottomNav.getLayoutParams() instanceof androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) {
                androidx.constraintlayout.widget.ConstraintLayout.LayoutParams lp =
                    (androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) glassBottomNav.getLayoutParams();
                lp.bottomMargin = insets.bottom + dpToPx(16);
                glassBottomNav.setLayoutParams(lp);
            }
            return windowInsets;
        });
    }

    private void setupNavListeners() {
        navItemHome.setOnClickListener(v -> selectTab(TAB_HOME));
        navItemNetwork.setOnClickListener(v -> selectTab(TAB_NETWORK));
        navItemUsage.setOnClickListener(v -> selectTab(TAB_USAGE));
        navItemNotifications.setOnClickListener(v -> selectTab(TAB_NOTIFICATIONS));
        navItemAnalytics.setOnClickListener(v -> selectTab(TAB_ANALYTICS));
    }

    private void selectTab(int tab) {
        currentTab = tab;
        Fragment fragment;

        switch (tab) {
            case TAB_NETWORK:
                fragment = new NetworkFragment();
                break;
            case TAB_USAGE:
                fragment = new UsageFragment();
                break;
            case TAB_NOTIFICATIONS:
                fragment = new NotificationsFragment();
                break;
            case TAB_ANALYTICS:
                fragment = new AnalyticsFragment();
                break;
            case TAB_HOME:
            default:
                fragment = new DashboardFragment();
                break;
        }

        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit();

        updateTabAppearance(tab);
    }

    private void updateTabAppearance(int activeTab) {
        resetTab(navItemHome, navIconHome, navLabelHome);
        resetTab(navItemNetwork, navIconNetwork, navLabelNetwork);
        resetTab(navItemUsage, navIconUsage, navLabelUsage);
        resetTab(navItemNotifications, navIconNotifications, navLabelNotifications);
        resetTab(navItemAnalytics, navIconAnalytics, navLabelAnalytics);

        switch (activeTab) {
            case TAB_HOME:
                activateTab(navItemHome, navIconHome, navLabelHome);
                break;
            case TAB_NETWORK:
                activateTab(navItemNetwork, navIconNetwork, navLabelNetwork);
                break;
            case TAB_USAGE:
                activateTab(navItemUsage, navIconUsage, navLabelUsage);
                break;
            case TAB_NOTIFICATIONS:
                activateTab(navItemNotifications, navIconNotifications, navLabelNotifications);
                break;
            case TAB_ANALYTICS:
                activateTab(navItemAnalytics, navIconAnalytics, navLabelAnalytics);
                break;
        }
    }

    private void activateTab(LinearLayout item, ImageView icon, TextView label) {
        item.setBackgroundResource(R.drawable.bg_glass_pill_active);
        int cyan = ContextCompat.getColor(this, R.color.accent_cyan);
        icon.setColorFilter(cyan);
        label.setTextColor(cyan);
        label.setTypeface(null, android.graphics.Typeface.BOLD);
    }

    private void resetTab(LinearLayout item, ImageView icon, TextView label) {
        item.setBackground(null);
        int muted = ContextCompat.getColor(this, R.color.text_muted);
        icon.setColorFilter(muted);
        label.setTextColor(muted);
        label.setTypeface(null, android.graphics.Typeface.NORMAL);
    }

    @Override
    public void onBackPressed() {
        if (currentTab != TAB_HOME) {
            selectTab(TAB_HOME);
        } else {
            super.onBackPressed();
        }
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}
