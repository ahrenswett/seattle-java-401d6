package com.ferreirae.buycheapstuff;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.amazonaws.amplify.generated.graphql.CreateBuyableItemMutation;
import com.amazonaws.amplify.generated.graphql.ListBuyableItemsQuery;
import com.amazonaws.amplify.generated.graphql.ListCollectionsQuery;
import com.amazonaws.amplify.generated.graphql.OnCreateBuyableItemSubscription;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.SignInUIOptions;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.amazonaws.mobileconnectors.appsync.AppSyncSubscriptionCall;
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.exception.ApolloException;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import type.CreateBuyableItemInput;

public class MainActivity extends AppCompatActivity implements BuyableItemAdapter.OnBuyableItemInteractionListener {

    private String enteredItemName = null;
    private static final String TAG = "ferreirae.MainActivity";

    private List<BuyableItem> buyableItems;

    //Instance  variable for recyclerView
    RecyclerView recyclerView;
    BuyableItemAdapter buyableItemAdapter;

    //AWS
    AWSAppSyncClient awsAppSyncClient;

    public void putDataOnPage(String data) {
        TextView headerTextView = findViewById(R.id.hiTextView);
        headerTextView.setText(data);
    }
    @Override
    protected void onResume() {
        super.onResume();
        // grab username from sharedprefs and use it to update the label
//        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
//        String username = prefs.getString("username", "user");

        String username = AWSMobileClient.getInstance().getUsername();

        TextView nameTextView = findViewById(R.id.hiTextView);
        nameTextView.setText("Hi, " + username + "!");




    }

    // gets called automatically when the MainActivity is created/shown for the first time
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // these will always be here, every time, thanks Android
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        AWSMobileClient.getInstance().initialize(getApplicationContext(), new com.amazonaws.mobile.client.Callback<UserStateDetails>() {
            @Override
            public void onResult(UserStateDetails result) {
                Log.i("ncarignan.login", result.getUserState().toString());
                if(result.getUserState().toString().equals("SIGNED_OUT")){
                    AWSMobileClient.getInstance().showSignIn(MainActivity.this,
                            SignInUIOptions.builder().backgroundColor(1).logo(R.drawable.tv_scooby_doo_2b).build(),
                            new com.amazonaws.mobile.client.Callback<UserStateDetails>() {
                                @Override
                                public void onResult(UserStateDetails result) {
                                    Log.i("ncarignan.signin", result.getUserState().toString());
                                }

                                @Override
                                public void onError(Exception e) {
                                    Log.e("ncarignan.signin", e.getMessage());
                                }
                            });
                }

            }

            @Override
            public void onError(Exception e) {
                Log.e("ncarignan.login", e.getMessage());
            }
        });

        // Build a connection to AWS
        awsAppSyncClient = AWSAppSyncClient.builder()
                .context(getApplicationContext())
                .awsConfiguration(new AWSConfiguration(getApplicationContext()))
                .build();

        // run graphql query for all data
        queryAllBuyableItems();

        // subscribe to future updates
        Log.i(TAG, "trying to subscribe");
        OnCreateBuyableItemSubscription subscription = OnCreateBuyableItemSubscription.builder().build();
        awsAppSyncClient.subscribe(subscription).execute(new AppSyncSubscriptionCall.Callback<OnCreateBuyableItemSubscription.Data>() {
            @Override
            public void onResponse(@Nonnull com.apollographql.apollo.api.Response<OnCreateBuyableItemSubscription.Data> response) {
                // hey you have something
                // AWS calls this method when a new BuyableItem is created
                Log.i(TAG, "new data added");
                final BuyableItem newItem = new BuyableItem(response.data().onCreateBuyableItem().title(), response.data().onCreateBuyableItem().priceInCents());
                Handler handler = new Handler(Looper.getMainLooper()) {
                    @Override
                    public void handleMessage(Message inputMessage) {
                        buyableItemAdapter.addItem(newItem);
                    }
                };

                handler.obtainMessage().sendToTarget();

            }

            @Override
            public void onFailure(@Nonnull ApolloException e) {
                Log.i(TAG, Arrays.toString(e.getStackTrace()));
                Log.i(TAG, e.getCause().getMessage());
            }

            @Override
            public void onCompleted() {
                // good job you subscribed gold star
                Log.i(TAG, "subscribed to buyable items");
            }
        });


        // render the buyable items to the screen, in the RecyclerView
        // https://developer.android.com/guide/topics/ui/layout/recyclerview
        // this.buyableItems will be empty until we get the data from GraphQL
        this.buyableItems = new LinkedList<>();
        recyclerView = findViewById(R.id.results);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        this.buyableItemAdapter = new BuyableItemAdapter(this.buyableItems, this);
        recyclerView.setAdapter(this.buyableItemAdapter);


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

                // tell graphql to add the item
                runAddBuyableItemMutation(enteredItemName);

                MainActivity.this.findViewById(R.id.results).setVisibility(View.VISIBLE);
            }
        });

        // Handle a button click for logout
        // need a listener
        // stuff happens in the listener (signout)
        // need to connect it to a button
        Button signoutButton = findViewById(R.id.signout);
        signoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View event) {
                String username = AWSMobileClient.getInstance().getUsername();

                AWSMobileClient.getInstance().signOut();

                TextView hiView = findViewById(R.id.hiTextView);
                hiView.setText("Bye " + username +"!");

//                MainActivity.this.();
            }
        });

        Button signinButton = findViewById(R.id.signin);
        signinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View event) {
                AWSMobileClient.getInstance().showSignIn(MainActivity.this,
                        SignInUIOptions.builder().backgroundColor(1).logo(R.drawable.tv_scooby_doo_2b).build(),
                        new com.amazonaws.mobile.client.Callback<UserStateDetails>() {
                    @Override
                    public void onResult(UserStateDetails result) {
                        Log.i("ncarignan.signin", result.getUserState().toString());
                        Log.i("ncarignan.signin", AWSMobileClient.getInstance().currentUserState().getUserState().toString());
                        Log.i("ncarignan.signin", AWSMobileClient.getInstance().currentUserState().getDetails().toString());
                        Log.i("ncarignan.signin", AWSMobileClient.getInstance().getUsername());
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e("ncarignan.signin", e.getMessage());
                    }
                });
            }
        });

    }


    // Code to insert a new item using a mutation
    // we pass in the fields to the CreateBuyableItemInput, create a mutation with that input, and then tell aws to run a mutation with it
    public void runAddBuyableItemMutation(String enteredItemName){
        CreateBuyableItemInput createBuyableItemInput =CreateBuyableItemInput.builder()
                .title(enteredItemName)
                .priceInCents(400000)
                .build();
        awsAppSyncClient.mutate(CreateBuyableItemMutation.builder().input(createBuyableItemInput).build())
            .enqueue(addBuyableItemCallBack);
    }

    // callback for when aws is done adding an item
    // graphql will return to us the thing that was created
    // it will live in response.data().createBuyableItem()
    public GraphQLCall.Callback<CreateBuyableItemMutation.Data> addBuyableItemCallBack = new GraphQLCall.Callback<CreateBuyableItemMutation.Data>() {
        @Override
        public void onResponse(@Nonnull final com.apollographql.apollo.api.Response<CreateBuyableItemMutation.Data> response) {
            Log.i("graphql insert", "added a buyable item");

            Handler handlerForMainThread = new Handler(Looper.getMainLooper()){
                @Override
                public void handleMessage(Message inputMessage){



                    // The code that we insert into the call back to do work for us
                    Log.i("graphql insert", "made it to the callback");
                    //grab the data
                    CreateBuyableItemMutation.CreateBuyableItem item = response.data().createBuyableItem();
                    //make a new buyable item with it
                    buyableItems.add(new BuyableItem(item));
                    recyclerView.getAdapter().notifyDataSetChanged();

                }
            };

            handlerForMainThread.obtainMessage().sendToTarget();


        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e("graphql insert", e.getMessage());
        }
    };


    // Query the dynamo db
    public void queryAllBuyableItems(){
        awsAppSyncClient.query(ListBuyableItemsQuery.builder().build())
                .responseFetcher(AppSyncResponseFetchers.CACHE_AND_NETWORK)
                .enqueue(getAllBuyableItemsCallback);
    }

    public GraphQLCall.Callback<ListBuyableItemsQuery.Data> getAllBuyableItemsCallback = new GraphQLCall.Callback<ListBuyableItemsQuery.Data>() {
        @Override
        public void onResponse(@Nonnull final com.apollographql.apollo.api.Response<ListBuyableItemsQuery.Data> response) {
            Log.i("graphqlgetall", response.data().listBuyableItems().items().toString());

            // Instead of declaring an external handler class, declare one in the onResponse
            // this will have access to the instance variables of our MainActivity
            Handler handlerForMainThread = new Handler(Looper.getMainLooper()){
                @Override
                public void handleMessage(Message inputMessage){

                    // The code that actually displays things to the screem
                    Log.i("graphqlgetall", "made it to the callback");
                    List<ListBuyableItemsQuery.Item> items = response.data().listBuyableItems().items();
                    buyableItems.clear();
                    for(ListBuyableItemsQuery.Item item : items){
                        buyableItems.add(new BuyableItem(item));
                    }
                    recyclerView.getAdapter().notifyDataSetChanged();


                }
            };

            handlerForMainThread.obtainMessage().sendToTarget();


        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e("graphqlgetall", e.getMessage());
        }
    };



    public void goToSettingsActivity(View v) {
        Intent i = new Intent(this, SettingsActivity.class);
        this.startActivity(i);
    }

    public void goToCollectionsActivity(View v) {
        Intent i = new Intent(this, CollectionActivity.class);
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

class LogDataWhenItComesBackCallback implements Callback {

    MainActivity actualMainActivityInstance;

    public LogDataWhenItComesBackCallback(MainActivity actualMainActivityInstance) {
        this.actualMainActivityInstance = actualMainActivityInstance;
    }
    private static final String TAG = "ferreirae.Callback";
    // OkHttp will call this if the request fails
    @Override
    public void onFailure(@NotNull Call call, @NotNull IOException e) {
        Log.e(TAG, "internet error");
        Log.e(TAG, e.getMessage());
    }

    // OkHttp will call this if the request succeeds
    @Override
    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
        String responseBody = response.body().string();
        Log.i(TAG, responseBody);
        //actualMainActivityInstance.putDataOnPage(responseBody);
        // defining a class that extends Handler with the curly braces!
        Handler handlerForMainThread = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message inputMessage) {
                // grab data out of Message object and pass to actualMainActivityInstance
                actualMainActivityInstance.putDataOnPage((String)inputMessage.obj);
            }
        };
        Message completeMessage =
                handlerForMainThread.obtainMessage(0, responseBody);
        completeMessage.sendToTarget();
    }
}
