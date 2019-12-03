package com.ferreirae.buycheapstuff;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements BuyableItemAdapter.OnBuyableItemInteractionListener {

    private String enteredItemName = null;

    private List<BuyableItem> buyableItems;

    @Override
    protected void onResume() {
        super.onResume();
        // grab username from sharedprefs and use it to update the label
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String username = prefs.getString("username", "user");
        TextView nameTextView = findViewById(R.id.hiTextView);
        nameTextView.setText("Hi, " + username + "!");
    }

    // gets called automatically when the MainActivity is created/shown for the first time
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // these will always be here, every time, thanks Android
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.buyableItems = new LinkedList<>();
        buyableItems.add(new BuyableItem("gold paperclip", 1000000));
        buyableItems.add(new BuyableItem("silver paperclip", 800000));
        buyableItems.add(new BuyableItem("bronze paperclip", 600000));

        // render the buyable items to the screen, in the RecyclerView
        // https://developer.android.com/guide/topics/ui/layout/recyclerview
        RecyclerView recyclerView = findViewById(R.id.results);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new BuyableItemAdapter(this.buyableItems, this));

        // when the button is clicked, show the thing whose id is results

        // grab the button, using its ID and the generated R (resource) info
        Button button = findViewById(R.id.button);
        // add the event listener to the button
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View event) {
                // hide keyboard
                InputMethodManager inputManager = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow((null == getCurrentFocus()) ? null : getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                // update text of the thing to be whatever was typed in

                // grab what was typed in
                EditText editText = findViewById(R.id.editText);
                enteredItemName = editText.getText().toString();
                // set the text of the thing to be our buyable items
//                TextView buyableItemsTextView = findViewById(R.id.itemTitle);
//                buyableItemsTextView.setText(MainActivity.this.buyableItems.toString());

                // show the results
                System.out.println("it was clicked!");
                MainActivity.this.findViewById(R.id.results).setVisibility(View.VISIBLE);
            }
        });

        // set up event listener for going to other activity
//        Button buyActivityButton = findViewById(R.id.goToBuyActivityButton);
//        buyActivityButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View event) {
//                // go to the other activity
//                // Create the intent to go to that activity and start it!
//                Intent goToBuyActivityIntent = new Intent(MainActivity.this, BuyItem.class);
//
//                // add some extra info about exactly what thing is being purchased
//                goToBuyActivityIntent.putExtra("item", enteredItemName);
//                MainActivity.this.startActivity(goToBuyActivityIntent);
//
////                Intent shareThingToBuyIntent = new Intent(Intent.ACTION_SEND);
////                EditText editText = findViewById(R.id.editText);
////                String textThatWasTypedIn = editText.getText().toString();
////                shareThingToBuyIntent.putExtra(Intent.EXTRA_TEXT, textThatWasTypedIn);
////                shareThingToBuyIntent.setType("text/plain");
////                MainActivity.this.startActivity(Intent.createChooser(shareThingToBuyIntent, "buy with..."));
//
//            }
//        });
    }

    public void goToSettingsActivity(View v) {
        Intent i = new Intent(this, SettingsActivity.class);
        this.startActivity(i);
    }

    @Override
    public void potato(BuyableItem item) {
        Intent goToBuyActivityIntent = new Intent(this, BuyItem.class);

        // add some extra info about exactly what thing is being purchased
        goToBuyActivityIntent.putExtra("item", item.getTitle());
        MainActivity.this.startActivity(goToBuyActivityIntent);
    }
}
