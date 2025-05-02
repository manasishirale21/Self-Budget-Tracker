package com.example.selfbudgettracker;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.selfbudgettracker.Model.Data;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.Date;

public class DashboardFragment extends Fragment {

    private FloatingActionButton fab_main, fab_income, fab_expense;

    private boolean isOpen = false;
    private Animation fadeOpen, fadeClose;

    private TextView totalIncome;
    private TextView totalExpense;

    private FirebaseAuth mAuth;
    private DatabaseReference mIncomeDatabase;
    private DatabaseReference mExpensedatabase;
    private RecyclerView mRecyclerIncome;
    private RecyclerView mRecyclerExpense;

    private FirebaseRecyclerAdapter<Data, DashboardFragment.IncomeViewHolder> adapter;
    FirebaseRecyclerAdapter<Data, ExpenseViewHolder> expenseAdapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View myview = inflater.inflate(R.layout.fragment_dashboard, container, false);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser mUser = mAuth.getCurrentUser();
        String uid = mUser.getUid();

        mIncomeDatabase = FirebaseDatabase.getInstance().getReference().child("IncomeData").child(uid);
        mExpensedatabase = FirebaseDatabase.getInstance().getReference().child("ExpenseData").child(uid);

        mIncomeDatabase.keepSynced(true);
        mExpensedatabase.keepSynced(true);

        fab_main = myview.findViewById(R.id.fab_add_transaction);
        fab_income = myview.findViewById(R.id.fab_income);
        fab_expense = myview.findViewById(R.id.fab_expense);

        totalIncome=myview.findViewById(R.id.tvIncomeAmount);
        totalExpense=myview.findViewById(R.id.tvExpenseAmount);

        mRecyclerIncome=myview.findViewById(R.id.rvIncome);
        mRecyclerExpense=myview.findViewById(R.id.rvExpenses);

        fadeOpen = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_open);
        fadeClose = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_close);

        // Assign income and expense button listeners here
        fab_income.setOnClickListener(view -> incomedataInsert());
        fab_expense.setOnClickListener(view -> expenseDataInsert());

        fab_main.setOnClickListener(view -> toggleFAB());


        mIncomeDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                int totalSum=0;

                for(DataSnapshot mysnap:snapshot.getChildren())
                {
                    Data data=mysnap.getValue(Data.class);
                    totalSum+=data.getAmount();

                    String stResult=String.valueOf(totalSum);

                    totalIncome.setText(stResult+".00");

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        mExpensedatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int totalSum=0;

                for(DataSnapshot mysnap:snapshot.getChildren())
                {
                    Data data=mysnap.getValue(Data.class);
                    totalSum+=data.getAmount();

                    String stResult=String.valueOf(totalSum);

                    totalExpense.setText(stResult+".00");

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        LinearLayoutManager layoutManagerIncome=new LinearLayoutManager(getActivity(),LinearLayoutManager.HORIZONTAL,false);
        layoutManagerIncome.setStackFromEnd(true);
        layoutManagerIncome.setReverseLayout(true);
        mRecyclerIncome.setHasFixedSize(true);
        mRecyclerIncome.setLayoutManager(layoutManagerIncome);

        LinearLayoutManager layoutManagerExpense=new LinearLayoutManager(getActivity(),LinearLayoutManager.HORIZONTAL,false);
        layoutManagerExpense.setStackFromEnd(true);
        layoutManagerExpense.setReverseLayout(true);
        mRecyclerExpense.setHasFixedSize(true);
        mRecyclerExpense.setLayoutManager(layoutManagerExpense);


        return myview;
    }

    private void toggleFAB() {
        if (isOpen) {
            fab_income.startAnimation(fadeClose);
            fab_expense.startAnimation(fadeClose);
            fab_income.setClickable(false);
            fab_expense.setClickable(false);
            fab_income.setVisibility(View.GONE);
            fab_expense.setVisibility(View.GONE);
            isOpen = false;
        } else {
            fab_income.setVisibility(View.VISIBLE);
            fab_expense.setVisibility(View.VISIBLE);
            fab_income.startAnimation(fadeOpen);
            fab_expense.startAnimation(fadeOpen);
            fab_income.setClickable(true);
            fab_expense.setClickable(true);
            isOpen = true;
        }
    }

    public void incomedataInsert() {
        AlertDialog.Builder mydialog = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = LayoutInflater.from(getActivity());

        View myviewm = inflater.inflate(R.layout.custom_layout_for_insertdata, null);
        mydialog.setView(myviewm);

        final AlertDialog dialog = mydialog.create();
        dialog.setCancelable(false);

        EditText editAmount = myviewm.findViewById(R.id.editTextAmount);
        EditText editType = myviewm.findViewById(R.id.editTextType);
        EditText editNote = myviewm.findViewById(R.id.editTextNote);

        Button btnSave = myviewm.findViewById(R.id.buttonSave);
        Button btnCancel = myviewm.findViewById(R.id.buttonCancel);

        btnSave.setOnClickListener(view -> {
            String type = editType.getText().toString().trim();
            String amount = editAmount.getText().toString().trim();
            String note = editNote.getText().toString().trim();

            if (TextUtils.isEmpty(type)) {
                editType.setError("Required Field...");
                return;
            }
            if (TextUtils.isEmpty(amount)) {
                editAmount.setError("Required Field...");
                return;
            }
            if (TextUtils.isEmpty(note)) {
                editNote.setError("Required Field");
                return;
            }

            int ouramountint = Integer.parseInt(amount);
            String id = mIncomeDatabase.push().getKey();
            String mDate = DateFormat.getDateInstance().format(new Date());

            Data data = new Data(ouramountint, type, note, id, mDate);
            mIncomeDatabase.child(id).setValue(data);

            Toast.makeText(getActivity(), "Income Added", Toast.LENGTH_SHORT).show();
            toggleFAB();
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(view -> {
            toggleFAB();
            dialog.dismiss();
        });

        dialog.show();
    }

    public void expenseDataInsert() {
        AlertDialog.Builder mydialog = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = LayoutInflater.from(getActivity());

        View myviewm = inflater.inflate(R.layout.custom_layout_for_insertdata, null);
        mydialog.setView(myviewm);

        final AlertDialog dialog = mydialog.create();
        dialog.setCancelable(false);

        EditText editAmount = myviewm.findViewById(R.id.editTextAmount);
        EditText editType = myviewm.findViewById(R.id.editTextType);
        EditText editNote = myviewm.findViewById(R.id.editTextNote);

        Button btnSave = myviewm.findViewById(R.id.buttonSave);
        Button btnCancel = myviewm.findViewById(R.id.buttonCancel);

        btnSave.setOnClickListener(view -> {
            String type = editType.getText().toString().trim();
            String amount = editAmount.getText().toString().trim();
            String note = editNote.getText().toString().trim();

            if (TextUtils.isEmpty(type)) {
                editType.setError("Required Field...");
                return;
            }
            if (TextUtils.isEmpty(amount)) {
                editAmount.setError("Required Field...");
                return;
            }
            if (TextUtils.isEmpty(note)) {
                editNote.setError("Required Field");
                return;
            }

            int ouramountint = Integer.parseInt(amount);
            String id = mExpensedatabase.push().getKey();
            String mDate = DateFormat.getDateInstance().format(new Date());

            Data data = new Data(ouramountint, type, note, id, mDate);
            mExpensedatabase.child(id).setValue(data);

            Toast.makeText(getActivity(), "Expense Added", Toast.LENGTH_SHORT).show();
            toggleFAB();
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(view -> {
            toggleFAB();
            dialog.dismiss();
        });

        dialog.show();
    }


    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Data> options =
                new FirebaseRecyclerOptions.Builder<Data>()
                        .setQuery(mIncomeDatabase, Data.class)
                        .build();

        adapter = new FirebaseRecyclerAdapter<Data, IncomeViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull IncomeViewHolder holder, int position, @NonNull Data model) {
                holder.setIncomeType(model.getType());
                holder.setincomeAmount(model.getAmount());
                holder.setIncomeDate(model.getDate());
            }

            @NonNull
            @Override
            public IncomeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.dashboard_income, parent, false);
                return new IncomeViewHolder(view);
            }
        };

        mRecyclerIncome.setAdapter(adapter);
        adapter.startListening();


        FirebaseRecyclerOptions<Data> expenseOptions =
                new FirebaseRecyclerOptions.Builder<Data>()
                        .setQuery(mExpensedatabase, Data.class)
                        .build();
       expenseAdapter = new FirebaseRecyclerAdapter<Data, ExpenseViewHolder>(expenseOptions) {
                    @Override
                    protected void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position, @NonNull Data model) {
                        holder.setExpenseType(model.getType());
                        holder.setmExpenseAmount(model.getAmount());
                        holder.setExpenseDate(model.getDate());
                    }

                    @NonNull
                    @Override
                    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.dashboard_expense, parent, false);
                        return new ExpenseViewHolder(view);
                    }
                };

        mRecyclerExpense.setAdapter(expenseAdapter);
        expenseAdapter.startListening();
    }


    public static class IncomeViewHolder extends RecyclerView.ViewHolder{

        View mIncomeView;

        public IncomeViewHolder(@NonNull View itemView) {
            super(itemView);
            mIncomeView=itemView;
        }

        public void setIncomeType(String type)
        {
            TextView mtype=mIncomeView.findViewById(R.id.type_income);
            mtype.setText(type);
        }
        public void setincomeAmount(int amount)
        {
            TextView mamount=mIncomeView.findViewById(R.id.amount_income);
            String stAmmount=String.valueOf(amount);
            mamount.setText(stAmmount);
        }
        public void setIncomeDate(String date)
        {
            TextView mDate=mIncomeView.findViewById(R.id.date_income);
            mDate.setText(date);
        }
    }

    public static class ExpenseViewHolder extends RecyclerView.ViewHolder{

        View mExpenseView;

        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            mExpenseView=itemView;
        }

        public void setExpenseType(String type)
        {
            TextView mtype=mExpenseView.findViewById(R.id.type_expense);
            mtype.setText(type);
        }
        public void setmExpenseAmount(int amount)
        {
            TextView mamount=mExpenseView.findViewById(R.id.amount_expense);
            String stAmmount=String.valueOf(amount);
            mamount.setText(stAmmount);
        }
        public void setExpenseDate(String date)
        {
            TextView mDate=mExpenseView.findViewById(R.id.date_expense);
            mDate.setText(date);
        }
    }
}
