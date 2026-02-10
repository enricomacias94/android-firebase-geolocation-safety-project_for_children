package com.ilanhit.myapplication.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.function.Consumer;

import com.ilanhit.myapplication.R;
import com.ilanhit.myapplication.models.Child;
import com.ilanhit.myapplication.models.Person;

public class ContactsAdapter<T extends Person> extends RecyclerView.Adapter<ContactsAdapter<T>.PersonViewHolder> {
    private List<T> personList;
    private Consumer<T> onLocation;
//display liste des contacts
    public ContactsAdapter(List<T> personList, Consumer<T> onLocation){
        this.personList = personList;
        this.onLocation = onLocation;
    }

    @NonNull
    @Override
    public PersonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.person_item, parent, false);
        return new PersonViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PersonViewHolder holder, int position) {
        T person = personList.get(position);
        holder.bind(person);
    }

    @Override
    public int getItemCount() {
        return personList.size();
    }

    public class PersonViewHolder extends RecyclerView.ViewHolder{
        private TextView tvItemFirstName;
        private TextView tvItemLastName;
        private TextView tvItemPhone;
        private TextView tvItemEmail;
        private Button btnShowLocation;

        public PersonViewHolder(@NonNull View itemView) {
            super(itemView);
            tvItemFirstName = itemView.findViewById(R.id.tvItemFirstName);
            tvItemLastName = itemView.findViewById(R.id.tvItemLastName);
            tvItemPhone = itemView.findViewById(R.id.tvItemPhone);
            tvItemEmail = itemView.findViewById(R.id.tvItemEmail);
            btnShowLocation = itemView.findViewById(R.id.btnShowLocation);
        }
        public void bind(T person){
            tvItemFirstName.setText(person.getFirstName());
            tvItemLastName.setText(person.getLastName());
            tvItemPhone.setText(person.getPhone());
            tvItemEmail.setText(person.getEmail());
            btnShowLocation.setOnClickListener( v -> onLocation.accept(person));
        }
    }
}
