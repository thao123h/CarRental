package com.example.carrental.activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.carrental.R;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AddCarActivity extends AppCompatActivity {

    private static final String ADD_CAR_URL = "http://10.0.2.2:8082/item/addCar";

    private ViewPager2 viewPager;
    private Button btnPrev, btnNext;
    private FormPagerAdapter adapter;

    // Step indicators
    private View stepCircleInfo, stepCircleImages, stepCircleDocs, stepCirclePrice;
    private TextView stepLabelInfo, stepLabelImages, stepLabelDocs, stepLabelPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_car);

        viewPager = findViewById(R.id.view_pager);
        btnPrev = findViewById(R.id.btn_prev);
        btnNext = findViewById(R.id.btn_next);

        // Step indicators
        stepCircleInfo = findViewById(R.id.step_circle_info);
        stepCircleImages = findViewById(R.id.step_circle_images);
        stepCircleDocs = findViewById(R.id.step_circle_documents);
        stepCirclePrice = findViewById(R.id.step_circle_price);
        stepLabelInfo = findViewById(R.id.step_label_info);
        stepLabelImages = findViewById(R.id.step_label_images);
        stepLabelDocs = findViewById(R.id.step_label_documents);
        stepLabelPrice = findViewById(R.id.step_label_price);

        List<Fragment> pages = new ArrayList<>();
        pages.add(new CarInfoFragment());
        pages.add(new CarImagesFragment());
        pages.add(new CarDocumentsFragment());
        pages.add(new CarPriceFragment());

        adapter = new FormPagerAdapter(this, pages);
        viewPager.setAdapter(adapter);
        viewPager.setUserInputEnabled(false);

        updateButtons();
        updateSteps(0);

        btnPrev.setOnClickListener(v -> {
            int pos = viewPager.getCurrentItem();
            if (pos > 0) {
                viewPager.setCurrentItem(pos - 1);
                updateButtons();
                updateSteps(pos - 1);
            }
        });

        btnNext.setOnClickListener(v -> {
            int pos = viewPager.getCurrentItem();
            if (pos < adapter.getItemCount() - 1) {
                Fragment f = adapter.getFragmentAt(pos);
                if (f instanceof PageFragment) {
                    boolean ok = ((PageFragment) f).validatePage();
                    if (!ok) return;
                }
                viewPager.setCurrentItem(pos + 1);
                updateButtons();
                updateSteps(pos + 1);
            } else {
                submitAll();
            }
        });
    }

    private void updateButtons() {
        int pos = viewPager.getCurrentItem();
        btnPrev.setEnabled(pos > 0);
        if (pos == adapter.getItemCount() - 1)
            btnNext.setText("Gửi");
        else
            btnNext.setText("Tiếp theo");
    }

    /** 🔄 Cập nhật màu cho step indicator theo vị trí hiện tại */
    private void updateSteps(int pos) {
        int active = getColor(R.color.primary);
        int inactive = getColor(R.color.light_grey);

        stepCircleInfo.setBackgroundTintList(android.content.res.ColorStateList.valueOf(inactive));
        stepCircleImages.setBackgroundTintList(android.content.res.ColorStateList.valueOf(inactive));
        stepCircleDocs.setBackgroundTintList(android.content.res.ColorStateList.valueOf(inactive));
        stepCirclePrice.setBackgroundTintList(android.content.res.ColorStateList.valueOf(inactive));

        switch (pos) {
            case 0: stepCircleInfo.setBackgroundTintList(android.content.res.ColorStateList.valueOf(active)); break;
            case 1: stepCircleImages.setBackgroundTintList(android.content.res.ColorStateList.valueOf(active)); break;
            case 2: stepCircleDocs.setBackgroundTintList(android.content.res.ColorStateList.valueOf(active)); break;
            case 3: stepCirclePrice.setBackgroundTintList(android.content.res.ColorStateList.valueOf(active)); break;
        }
    }

    private void submitAll() {
        try {
            JSONObject carJson = new JSONObject();

            for (int i = 0; i < adapter.getItemCount(); i++) {
                Fragment f = adapter.getFragmentAt(i);
                if (f instanceof PageFragment) {
                    PageFragment pf = (PageFragment) f;
                    if (!pf.validatePage()) {
                        viewPager.setCurrentItem(i);
                        updateButtons();
                        updateSteps(i);
                        return;
                    }
                    pf.putData(carJson);
                }
            }

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, ADD_CAR_URL, carJson,
                    response -> {
                        Toast.makeText(this, "Thêm xe thành công!", Toast.LENGTH_SHORT).show();
                        finish();
                    },
                    error -> {
                        error.printStackTrace();
                        Toast.makeText(this, "Lỗi khi thêm xe!", Toast.LENGTH_SHORT).show();
                    });

            Volley.newRequestQueue(this).add(request);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Dữ liệu không hợp lệ!", Toast.LENGTH_SHORT).show();
        }
    }

    // ---------- Adapter ----------
    private static class FormPagerAdapter extends FragmentStateAdapter {
        private final List<Fragment> fragments;

        public FormPagerAdapter(@NonNull AppCompatActivity fa, List<Fragment> fragments) {
            super(fa);
            this.fragments = fragments;
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) { return fragments.get(position); }

        @Override
        public int getItemCount() { return fragments.size(); }

        public Fragment getFragmentAt(int pos) { return fragments.get(pos); }
    }

    // ---------- Interface ----------
    public interface PageFragment {
        boolean validatePage();
        void putData(JSONObject carJson) throws Exception;
    }

    // ---------- Fragment 1 ----------
    public static class CarInfoFragment extends androidx.fragment.app.Fragment implements PageFragment {
        private TextInputEditText etPlate, etKms, etItemTitle;
        private Spinner spinnerBrand, spinnerModel, spinnerYear, spinnerSeat, spinnerFuel;
        private RadioGroup rgTransmission;

        @Nullable
        @Override
        public android.view.View onCreateView(@NonNull android.view.LayoutInflater inflater, @Nullable android.view.ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_car_info, container, false);
        }

        @Override
        public void onViewCreated(@NonNull android.view.View v, @Nullable Bundle savedInstanceState) {
            etPlate = v.findViewById(R.id.et_plate);
            spinnerBrand = v.findViewById(R.id.spinner_brand);
            spinnerModel = v.findViewById(R.id.spinner_model);
            spinnerYear = v.findViewById(R.id.spinner_year);
            spinnerSeat = v.findViewById(R.id.spinner_seat);
            spinnerFuel = v.findViewById(R.id.spinner_fuel);
            rgTransmission = v.findViewById(R.id.rg_transmission);
        }

        @Override
        public boolean validatePage() {
            String plate = etPlate.getText() == null ? "" : etPlate.getText().toString().trim();
            if (plate.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng nhập biển số", Toast.LENGTH_SHORT).show();
                return false;
            }
            return true;
        }

        @Override
        public void putData(JSONObject carJson) throws Exception {
            carJson.put("licensePlate", etPlate.getText().toString());
        }
    }

    // ---------- Fragment 2 ----------
    public static class CarImagesFragment extends androidx.fragment.app.Fragment implements PageFragment {
        private final List<String> imageUris = new ArrayList<>();
        private final ImageView[] slots = new ImageView[2];
        private int currentSlot = -1;
        private ActivityResultLauncher<String[]> openDocumentLauncher;

        @Override
        public void onAttach(@NonNull Context context) {
            super.onAttach(context);
            openDocumentLauncher = registerForActivityResult(new ActivityResultContracts.OpenDocument(),
                    (ActivityResultCallback<Uri>) result -> {
                        if (result != null && currentSlot >= 0) {
                            slots[currentSlot].setImageURI(result);
                            imageUris.set(currentSlot, result.toString());
                            currentSlot = -1;
                        }
                    });
        }

        @Nullable
        @Override
        public android.view.View onCreateView(@NonNull android.view.LayoutInflater inflater, @Nullable android.view.ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_car_images, container, false);
        }

        @Override
        public void onViewCreated(@NonNull android.view.View v, @Nullable Bundle savedInstanceState) {
            slots[0] = v.findViewById(R.id.iv_main);
            slots[1] = v.findViewById(R.id.iv_slot_1);
            imageUris.clear();
            imageUris.add(null);
            imageUris.add(null);

            for (int i = 0; i < slots.length; i++) {
                final int idx = i;
                slots[i].setOnClickListener(x -> {
                    currentSlot = idx;
                    openDocumentLauncher.launch(new String[]{"image/*"});
                });
            }
        }

        @Override public boolean validatePage() { return true; }
        @Override public void putData(JSONObject carJson) throws Exception {
            JSONArray imgs = new JSONArray();
            for (String s : imageUris) if (s != null) imgs.put(s);
            carJson.put("itemImages", imgs);
        }
    }

    // ---------- Fragment 3 ----------
    public static class CarDocumentsFragment extends androidx.fragment.app.Fragment implements PageFragment {
        private TextInputEditText etRegistrationNumber;
        private ImageView ivDoc1, ivDoc2;
        private final List<String> docUris = new ArrayList<>();
        private ActivityResultLauncher<String[]> openDocumentLauncher;
        private int currentDoc = -1;

        @Override
        public void onAttach(@NonNull Context context) {
            super.onAttach(context);
            openDocumentLauncher = registerForActivityResult(new ActivityResultContracts.OpenDocument(),
                    result -> {
                        if (result != null && currentDoc >= 0) {
                            docUris.set(currentDoc, result.toString());
                            currentDoc = -1;
                        }
                    });
        }

        @Nullable
        @Override
        public android.view.View onCreateView(@NonNull android.view.LayoutInflater inflater, @Nullable android.view.ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_car_documents, container, false);
        }

        @Override
        public void onViewCreated(@NonNull android.view.View v, @Nullable Bundle savedInstanceState) {
            etRegistrationNumber = v.findViewById(R.id.et_registration_number);
            ivDoc1 = v.findViewById(R.id.iv_doc_1);
            ivDoc2 = v.findViewById(R.id.iv_doc_2);
            docUris.add(null); docUris.add(null);

            ivDoc1.setOnClickListener(x -> { currentDoc = 0; openDocumentLauncher.launch(new String[]{"image/*"}); });
            ivDoc2.setOnClickListener(x -> { currentDoc = 1; openDocumentLauncher.launch(new String[]{"image/*"}); });
        }

        @Override public boolean validatePage() { return true; }
        @Override public void putData(JSONObject carJson) throws Exception {
            JSONArray docsArr = new JSONArray();
            for (String s : docUris) if (s != null) docsArr.put(s);
            carJson.put("documents", docsArr);
        }
    }

    // ---------- Fragment 4 ----------
    public static class CarPriceFragment extends androidx.fragment.app.Fragment implements PageFragment {
        private TextInputEditText etPriceSelf, etPriceWith;

        @Nullable
        @Override
        public android.view.View onCreateView(@NonNull android.view.LayoutInflater inflater, @Nullable android.view.ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_car_price, container, false);
        }

        @Override
        public void onViewCreated(@NonNull android.view.View v, @Nullable Bundle savedInstanceState) {
            etPriceSelf = v.findViewById(R.id.et_price_self);
            etPriceWith = v.findViewById(R.id.et_price_with_driver);
        }

        @Override public boolean validatePage() { return true; }

        @Override
        public void putData(JSONObject carJson) throws Exception {
            JSONObject item = new JSONObject();
            String pSelf = etPriceSelf.getText() == null ? "" : etPriceSelf.getText().toString();
            String pWith = etPriceWith.getText() == null ? "" : etPriceWith.getText().toString();
            if (!pSelf.isEmpty()) item.put("price", Integer.parseInt(pSelf));
            if (!pWith.isEmpty()) item.put("priceWithDriver", Integer.parseInt(pWith));
            carJson.put("item", item);
        }
    }
}
