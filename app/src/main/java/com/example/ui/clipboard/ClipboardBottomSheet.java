package com.example.ui.clipboard;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.R;
import com.example.database.entity.ClipboardEntity;
import com.example.repository.ClipboardRepository;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;

public class ClipboardBottomSheet extends BottomSheetDialogFragment {
    private ClipboardRepository clipboardRepo;
    private ClipboardAdapter adapter;

    private TextView tabTemp;
    private TextView tabFav;
    private TextView tabPerm;
    private EditText etSearch;
    private TextView tvEmpty;
    private RecyclerView rvClipboard;
    private ImageView btnAdd;

    private String currentState = ClipboardEntity.STATE_TEMPORARY;
    private List<ClipboardEntity> cachedList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_clipboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        clipboardRepo = new ClipboardRepository(requireContext());

        initViews(view);
        setupRecyclerView();
        setupListeners();
        selectTab(ClipboardEntity.STATE_TEMPORARY);
    }

    private void initViews(View view) {
        tabTemp = view.findViewById(R.id.tab_clip_temp);
        tabFav = view.findViewById(R.id.tab_clip_fav);
        tabPerm = view.findViewById(R.id.tab_clip_perm);
        etSearch = view.findViewById(R.id.et_search_clipboard);
        tvEmpty = view.findViewById(R.id.empty_clipboard_text);
        rvClipboard = view.findViewById(R.id.rv_clipboard);
        btnAdd = view.findViewById(R.id.btn_add_clipboard_entry);
    }

    private void setupRecyclerView() {
        adapter = new ClipboardAdapter(new ClipboardAdapter.OnClipboardActionListener() {
            @Override
            public void onCopy(ClipboardEntity item) {
                clipboardRepo.copyToSystemClipboard(item.content);
                Toast.makeText(getContext(), R.string.clipboard_copied, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onTogglePin(ClipboardEntity item) {
                if (ClipboardEntity.STATE_TEMPORARY.equals(item.storageState)) {
                    item.storageState = ClipboardEntity.STATE_FAVORITE;
                    Toast.makeText(getContext(), "Pinned to Favorites", Toast.LENGTH_SHORT).show();
                } else if (ClipboardEntity.STATE_FAVORITE.equals(item.storageState)) {
                    item.storageState = ClipboardEntity.STATE_PERMANENT;
                    Toast.makeText(getContext(), "Moved to Permanent", Toast.LENGTH_SHORT).show();
                } else {
                    item.storageState = ClipboardEntity.STATE_TEMPORARY;
                    Toast.makeText(getContext(), "Moved to Temporary (24h)", Toast.LENGTH_SHORT).show();
                }
                clipboardRepo.update(item);
            }

            @Override
            public void onDelete(ClipboardEntity item) {
                clipboardRepo.delete(item);
                Toast.makeText(getContext(), "Entry deleted", Toast.LENGTH_SHORT).show();
            }
        });

        rvClipboard.setLayoutManager(new LinearLayoutManager(getContext()));
        rvClipboard.setAdapter(adapter);
    }

    private void setupListeners() {
        tabTemp.setOnClickListener(v -> selectTab(ClipboardEntity.STATE_TEMPORARY));
        tabFav.setOnClickListener(v -> selectTab(ClipboardEntity.STATE_FAVORITE));
        tabPerm.setOnClickListener(v -> selectTab(ClipboardEntity.STATE_PERMANENT));

        btnAdd.setOnClickListener(v -> showManualAddDialog());

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterList(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void selectTab(String state) {
        currentState = state;
        if (getContext() == null) return;

        tabTemp.setBackgroundResource(ClipboardEntity.STATE_TEMPORARY.equals(state) ? R.drawable.bg_glass_pill_active : R.drawable.bg_glass_pill);
        tabTemp.setTextColor(ContextCompat.getColor(getContext(), ClipboardEntity.STATE_TEMPORARY.equals(state) ? R.color.accent_cyan : R.color.text_muted));

        tabFav.setBackgroundResource(ClipboardEntity.STATE_FAVORITE.equals(state) ? R.drawable.bg_glass_pill_active : R.drawable.bg_glass_pill);
        tabFav.setTextColor(ContextCompat.getColor(getContext(), ClipboardEntity.STATE_FAVORITE.equals(state) ? R.color.accent_cyan : R.color.text_muted));

        tabPerm.setBackgroundResource(ClipboardEntity.STATE_PERMANENT.equals(state) ? R.drawable.bg_glass_pill_active : R.drawable.bg_glass_pill);
        tabPerm.setTextColor(ContextCompat.getColor(getContext(), ClipboardEntity.STATE_PERMANENT.equals(state) ? R.color.accent_cyan : R.color.text_muted));

        clipboardRepo.getEntriesByState(state).observe(getViewLifecycleOwner(), list -> {
            cachedList = list != null ? list : new ArrayList<>();
            filterList(etSearch.getText().toString());
        });
    }

    private void filterList(String query) {
        List<ClipboardEntity> filtered = new ArrayList<>();
        if (query == null || query.trim().isEmpty()) {
            filtered.addAll(cachedList);
        } else {
            String lower = query.toLowerCase();
            for (ClipboardEntity item : cachedList) {
                if (item.content.toLowerCase().contains(lower)) {
                    filtered.add(item);
                }
            }
        }

        if (filtered.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            rvClipboard.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            rvClipboard.setVisibility(View.VISIBLE);
            adapter.submitList(filtered);
        }
    }

    private void showManualAddDialog() {
        if (getContext() == null) return;
        EditText input = new EditText(getContext());
        input.setHint("Paste or type text to save...");
        input.setTextColor(ContextCompat.getColor(getContext(), R.color.text_primary));
        input.setHintTextColor(ContextCompat.getColor(getContext(), R.color.text_muted));
        input.setPadding(32, 24, 32, 24);

        // Pre-fill with system clipboard if available
        String currentClip = clipboardRepo.getCurrentSystemClipboardText();
        if (currentClip != null) input.setText(currentClip);

        new AlertDialog.Builder(getContext(), R.style.Theme_DigitalPulse)
            .setTitle(R.string.add_clipboard_item)
            .setView(input)
            .setPositiveButton("Save", (dialog, which) -> {
                String text = input.getText().toString().trim();
                if (!text.isEmpty()) {
                    clipboardRepo.saveClipboardText(text, currentState);
                    Toast.makeText(getContext(), "Saved to " + currentState, Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
}
