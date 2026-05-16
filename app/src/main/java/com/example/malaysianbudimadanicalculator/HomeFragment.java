package com.example.malaysianbudimadanicalculator;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Locale;

public class HomeFragment extends Fragment {

    private static final double BUDI_SUBSIDY_RATE = 1.99;

    private Spinner spinnerPetrolType;
    private TextInputEditText etMainInput, etPetrolPrice;
    private MaterialSwitch switchBudi;
    private Button btnCalculate;

    private TextView tvTotalCost, tvAmountPaid, tvFuelPriceLabel;
    private View cardSummary;
    private TextView tvSummaryLiters, tvSummaryPumpPrice, tvSummaryBudiPrice;

    public HomeFragment() {
        // Required empty constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home2, container, false);

        spinnerPetrolType = view.findViewById(R.id.spinnerPetrolType);
        etMainInput = view.findViewById(R.id.etMainInput);
        etPetrolPrice = view.findViewById(R.id.etPetrolPrice);
        switchBudi = view.findViewById(R.id.switchBudi);
        btnCalculate = view.findViewById(R.id.btnCalculate);

        tvTotalCost = view.findViewById(R.id.tvTotalCost);
        tvAmountPaid = view.findViewById(R.id.tvAmountPaid);
        tvFuelPriceLabel = view.findViewById(R.id.tvFuelPriceLabel);
        cardSummary = view.findViewById(R.id.cardSummary);


        tvSummaryLiters = view.findViewById(R.id.tvSummaryLiters);
        tvSummaryPumpPrice = view.findViewById(R.id.tvSummaryPumpPrice);
        tvSummaryBudiPrice = view.findViewById(R.id.tvSummaryBudiPrice);

        setupPetrolSpinner();


        btnCalculate.setOnClickListener(v -> calculatePetrolCost());

        switchBudi.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!TextUtils.isEmpty(etMainInput.getText()) && !TextUtils.isEmpty(etPetrolPrice.getText())) {
                calculatePetrolCost();
            }
        });

        return view;
    }

    private void setupPetrolSpinner() {
        String[] petrolTypes = {"RON95", "RON97", "Diesel"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                petrolTypes
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPetrolType.setAdapter(adapter);

        spinnerPetrolType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedType = parent.getItemAtPosition(position).toString();
                tvFuelPriceLabel.setText(selectedType + " price");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void calculatePetrolCost() {
        String inputText = etMainInput.getText().toString().trim();
        String priceText = etPetrolPrice.getText().toString().trim();

        if (TextUtils.isEmpty(inputText)) {
            etMainInput.setError("Required");
            etMainInput.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(priceText)) {
            etPetrolPrice.setError("Required");
            etPetrolPrice.requestFocus();
            return;
        }

        try {
            double inputValue = Double.parseDouble(inputText);
            double petrolPrice = Double.parseDouble(priceText);

            if (petrolPrice <= 0 || inputValue < 0) {
                Toast.makeText(requireContext(), "Please enter valid numbers > 0", Toast.LENGTH_SHORT).show();
                return;
            }

            String petrolType = spinnerPetrolType.getSelectedItem().toString();
            boolean isEligible = switchBudi.isChecked();

            double fuelUsage = inputValue;
            double totalPetrolCost = fuelUsage * petrolPrice;
            double budiRebate = 0.00;

            if (petrolType.equals("RON95") && isEligible) {
                if (petrolPrice > BUDI_SUBSIDY_RATE) {
                    budiRebate = (petrolPrice - BUDI_SUBSIDY_RATE) * fuelUsage;
                }
                tvSummaryBudiPrice.setText(String.format(Locale.getDefault(), "RM %.2f/L", BUDI_SUBSIDY_RATE));
            } else if (!petrolType.equals("RON95") && isEligible) {
                Toast.makeText(requireContext(), "BUDI MADANI is strictly for RON95 users.", Toast.LENGTH_SHORT).show();
                switchBudi.setChecked(false);
                tvSummaryBudiPrice.setText("Not Eligible");
                budiRebate = 0;
            } else {
                tvSummaryBudiPrice.setText("Not Applied");
                budiRebate = 0;
            }

            double finalPayable = totalPetrolCost - budiRebate;
            if (finalPayable < 0) finalPayable = 0;

            tvTotalCost.setText(String.format(Locale.getDefault(), "RM %.2f", totalPetrolCost));
            tvAmountPaid.setText(String.format(Locale.getDefault(), "RM %.2f", finalPayable));

            tvSummaryLiters.setText(String.format(Locale.getDefault(), "%.3f L", fuelUsage));
            tvSummaryPumpPrice.setText(String.format(Locale.getDefault(), "RM %.2f/L", petrolPrice));
            cardSummary.setVisibility(View.VISIBLE);

            if (budiRebate > 0) {
                Toast.makeText(requireContext(),
                        String.format(Locale.getDefault(), "Subsidy Applied! You saved RM %.2f", budiRebate),
                        Toast.LENGTH_LONG).show();
            }

        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "Invalid number format", Toast.LENGTH_SHORT).show();
        }
    }
}