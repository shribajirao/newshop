package wrteam.ecart.shop.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.messaging.FirebaseMessaging;
import com.paytm.pgsdk.PaytmPaymentTransactionCallback;
import com.razorpay.PaymentResultListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import wrteam.ecart.shop.R;
import wrteam.ecart.shop.fragment.AddressListFragment;
import wrteam.ecart.shop.fragment.CartFragment;
import wrteam.ecart.shop.fragment.CategoryFragment;
import wrteam.ecart.shop.fragment.DrawerFragment;
import wrteam.ecart.shop.fragment.FavoriteFragment;
import wrteam.ecart.shop.fragment.HomeFragment;
import wrteam.ecart.shop.fragment.OrderPlacedFragment;
import wrteam.ecart.shop.fragment.ProductDetailFragment;
import wrteam.ecart.shop.fragment.ProductListFragment;
import wrteam.ecart.shop.fragment.SubCategoryFragment;
import wrteam.ecart.shop.fragment.TrackOrderFragment;
import wrteam.ecart.shop.fragment.TrackerDetailFragment;
import wrteam.ecart.shop.fragment.WalletTransactionFragment;
import wrteam.ecart.shop.helper.ApiConfig;
import wrteam.ecart.shop.helper.Constant;
import wrteam.ecart.shop.helper.DatabaseHelper;
import wrteam.ecart.shop.helper.Session;

public class MainActivity extends AppCompatActivity implements PaymentResultListener, PaytmPaymentTransactionCallback {

    static final String TAG = "MAIN ACTIVITY";
    @SuppressLint("StaticFieldLeak")
    public static Toolbar toolbar;
    public static BottomNavigationView bottomNavigationView;
    public static Fragment active;
    public static FragmentManager fm = null;
    public static Fragment homeFragment, categoryFragment, favoriteFragment, trackOrderFragment, drawerFragment;
    public static boolean homeClicked = false, categoryClicked = false, favoriteClicked = false, drawerClicked = false;
    @SuppressLint("StaticFieldLeak")
    public static Activity activity;
    public Session session;
    boolean doubleBackToExitPressedOnce = false;
    Menu menu;
    DatabaseHelper databaseHelper;
    String from;
    TextView toolbarTitle;
    ImageView imageMenu, imageHome;
    CardView cardViewHamburger;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);

        toolbarTitle = findViewById(R.id.toolbarTitle);
        imageMenu = findViewById(R.id.imageMenu);
        imageHome = findViewById(R.id.imageHome);
        cardViewHamburger = findViewById(R.id.cardViewHamburger);

        activity = MainActivity.this;
        session = new Session(activity);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        from = getIntent().getStringExtra(Constant.FROM);
        databaseHelper = new DatabaseHelper(activity);

        if (session.getBoolean(Constant.IS_USER_LOGIN)) {
            ApiConfig.getCartItemCount(activity, session);
        } else {
            session.setData(Constant.STATUS, "1");
            databaseHelper.getTotalItemOfCart(activity);
        }

        setAppLocal("en"); //Change you language code here

        fm = getSupportFragmentManager();

        homeFragment = new HomeFragment();
        categoryFragment = new CategoryFragment();
        favoriteFragment = new FavoriteFragment();
        trackOrderFragment = new TrackOrderFragment();
        drawerFragment = new DrawerFragment();


        Bundle bundle = new Bundle();
        bottomNavigationView.setSelectedItemId(R.id.navMain);
        active = homeFragment;
        homeClicked = true;
        drawerClicked = false;
        favoriteClicked = false;
        categoryClicked = false;
        try {
            if (!getIntent().getStringExtra("json").isEmpty()) {
                bundle.putString("json", getIntent().getStringExtra("json"));
            }
            homeFragment.setArguments(bundle);
            fm.beginTransaction().add(R.id.container, homeFragment).commit();
        } catch (Exception e) {
            fm.beginTransaction().add(R.id.container, homeFragment).commit();
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            {
                switch (item.getItemId()) {
                    case R.id.navMain:
                        if (active != homeFragment) {
                            bottomNavigationView.getMenu().findItem(item.getItemId()).setChecked(true);
                            if (!homeClicked) {
                                fm.beginTransaction().add(R.id.container, homeFragment).show(homeFragment).hide(active).commit();
                                homeClicked = true;
                            } else {
                                fm.beginTransaction().show(homeFragment).hide(active).commit();
                            }
                            active = homeFragment;
                        }
                        break;
                    case R.id.navCategory:
                        if (active != categoryFragment) {
                            bottomNavigationView.getMenu().findItem(item.getItemId()).setChecked(true);
                            if (!categoryClicked) {
                                fm.beginTransaction().add(R.id.container, categoryFragment).show(categoryFragment).hide(active).commit();
                                categoryClicked = true;
                            } else {
                                fm.beginTransaction().show(categoryFragment).hide(active).commit();
                            }
                            active = categoryFragment;
                        }
                        break;
                    case R.id.navWishList:
                        if (active != favoriteFragment) {
                            bottomNavigationView.getMenu().findItem(item.getItemId()).setChecked(true);
                            if (!favoriteClicked) {
                                fm.beginTransaction().add(R.id.container, favoriteFragment).show(favoriteFragment).hide(active).commit();
                                favoriteClicked = true;
                            } else {
                                fm.beginTransaction().show(favoriteFragment).hide(active).commit();
                            }
                            active = favoriteFragment;
                        }
                        break;
                    case R.id.navProfile:
                        if (active != drawerFragment) {
                            bottomNavigationView.getMenu().findItem(item.getItemId()).setChecked(true);
                            if (!drawerClicked) {
                                fm.beginTransaction().add(R.id.container, drawerFragment).show(drawerFragment).hide(active).commit();
                                drawerClicked = true;
                            } else {
                                fm.beginTransaction().show(drawerFragment).hide(active).commit();
                            }
                            active = drawerFragment;
                        }
                        break;
                }
                return false;
            }
        });

        switch (from) {
            case "checkout":
                bottomNavigationView.setVisibility(View.GONE);
                ApiConfig.getCartItemCount(activity, session);
                Fragment fragment = new AddressListFragment();
                Bundle bundle00 = new Bundle();
                bundle00.putString(Constant.FROM, "login");
                bundle00.putDouble("total", Double.parseDouble(ApiConfig.StringFormat("" + Constant.FLOAT_TOTAL_AMOUNT)));
                fragment.setArguments(bundle00);
                fm.beginTransaction().add(R.id.container, fragment).addToBackStack(null).commit();
                break;
            case "share":
                Fragment fragment0 = new ProductDetailFragment();
                Bundle bundle0 = new Bundle();
                bundle0.putInt("variantPosition", getIntent().getIntExtra("variantPosition", 0));
                bundle0.putString("id", getIntent().getStringExtra("id"));
                bundle0.putString(Constant.FROM, "share");
                fragment0.setArguments(bundle0);
                fm.beginTransaction().add(R.id.container, fragment0).addToBackStack(null).commit();
                break;
            case "product":
                Fragment fragment1 = new ProductDetailFragment();
                Bundle bundle1 = new Bundle();
                bundle1.putInt("variantPosition", getIntent().getIntExtra("variantPosition", 0));
                bundle1.putString("id", getIntent().getStringExtra("id"));
                bundle1.putString(Constant.FROM, "product");
                fragment1.setArguments(bundle1);
                fm.beginTransaction().add(R.id.container, fragment1).addToBackStack(null).commit();
                break;
            case "category":
                Fragment fragment2 = new SubCategoryFragment();
                Bundle bundle2 = new Bundle();
                bundle2.putString("id", getIntent().getStringExtra("id"));
                bundle2.putString("name", getIntent().getStringExtra("name"));
                bundle2.putString(Constant.FROM, "category");
                fragment2.setArguments(bundle2);
                fm.beginTransaction().add(R.id.container, fragment2).addToBackStack(null).commit();
                break;
            case "order":
                Fragment fragment3 = new TrackerDetailFragment();
                Bundle bundle3 = new Bundle();
                bundle3.putSerializable("model", "");
                bundle3.putString("id", getIntent().getStringExtra("id"));
                fragment3.setArguments(bundle3);
                fm.beginTransaction().add(R.id.container, fragment3).addToBackStack(null).commit();
                break;
            case "tracker":
                fm.beginTransaction().add(R.id.container, new TrackOrderFragment()).addToBackStack(null).commit();
                break;
            case "payment_success":
                fm.beginTransaction().add(R.id.container, new OrderPlacedFragment()).addToBackStack(null).commit();
                break;
            case "wallet":
                fm.beginTransaction().add(R.id.container, new WalletTransactionFragment()).addToBackStack(null).commit();
                break;
        }

        fm.addOnBackStackChangedListener(() -> {
            toolbar.setVisibility(View.VISIBLE);
            Fragment currentFragment = fm.findFragmentById(R.id.container);
            assert currentFragment != null;
            currentFragment.onResume();
        });

        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(token -> {
            session.setData(Constant.FCM_ID, token);
            Register_FCM(token);
        });

        GetProductsName();
    }

    public void setAppLocal(String languageCode) {
        Resources resources = getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        Configuration configuration = resources.getConfiguration();
        configuration.setLocale(new Locale(languageCode.toLowerCase()));
        resources.updateConfiguration(configuration, dm);
        bottomNavigationView.setLayoutDirection(activity.getResources().getConfiguration().getLayoutDirection());
    }

    public void Register_FCM(String token) {
        Map<String, String> params = new HashMap<>();
        if (session.getBoolean(Constant.IS_USER_LOGIN)) {
            params.put(Constant.USER_ID, session.getData(Constant.USER_ID));
        }
        params.put(Constant.FCM_ID, token);

        ApiConfig.RequestToVolley((result, response) -> {
            if (result) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (!jsonObject.getBoolean(Constant.ERROR)) {
                        session.setData(Constant.FCM_ID, token);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();

                }

            }
        }, activity, Constant.REGISTER_DEVICE_URL, params, false);
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }
        this.doubleBackToExitPressedOnce = true;
        if (fm.getBackStackEntryCount() == 0) {
            if (active != homeFragment) {
                this.doubleBackToExitPressedOnce = false;
                bottomNavigationView.setSelectedItemId(R.id.navMain);
                homeClicked = true;
                fm.beginTransaction().hide(active).show(homeFragment).commit();
                active = homeFragment;
            } else {
                Toast.makeText(this, getString(R.string.exit_msg), Toast.LENGTH_SHORT).show();
                new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
            }

        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.toolbar_cart) {
            MainActivity.fm.beginTransaction().add(R.id.container, new CartFragment()).addToBackStack(null).commit();
        } else if (id == R.id.toolbar_search) {
            Fragment fragment = new ProductListFragment();
            Bundle bundle = new Bundle();
            bundle.putString(Constant.FROM, "search");
            bundle.putString(Constant.NAME, activity.getString(R.string.search));
            bundle.putString(Constant.ID, "");
            fragment.setArguments(bundle);
            MainActivity.fm.beginTransaction().add(R.id.container, fragment).addToBackStack(null).commit();
        } else if (id == R.id.toolbar_logout) {
            session.logoutUserConfirmation(activity);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.toolbar_cart).setVisible(true);
        menu.findItem(R.id.toolbar_search).setVisible(true);
        menu.findItem(R.id.toolbar_cart).setIcon(ApiConfig.buildCounterDrawable(Constant.TOTAL_CART_ITEM, activity));

        if (fm.getBackStackEntryCount() > 0) {
            toolbarTitle.setText(Constant.TOOLBAR_TITLE);
            bottomNavigationView.setVisibility(View.GONE);

            cardViewHamburger.setCardBackgroundColor(getColor(R.color.colorPrimaryLight));
            imageMenu.setOnClickListener(v -> fm.popBackStack());

            imageMenu.setVisibility(View.VISIBLE);
            imageHome.setVisibility(View.GONE);
        } else {
            if (session.getBoolean(Constant.IS_USER_LOGIN)) {
                toolbarTitle.setText(getString(R.string.hi) + session.getData(Constant.NAME) + "!");
            } else {
                toolbarTitle.setText(getString(R.string.hi_user));
            }
            bottomNavigationView.setVisibility(View.VISIBLE);
            cardViewHamburger.setCardBackgroundColor(getColor(R.color.transparent));
            imageMenu.setVisibility(View.GONE);
            imageHome.setVisibility(View.VISIBLE);
        }

        invalidateOptionsMenu();
        return super.onPrepareOptionsMenu(menu);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.container);
        Objects.requireNonNull(fragment).onActivityResult(requestCode, resultCode, data);

    }

    public void GetProductsName() {
        Map<String, String> params = new HashMap<>();
        params.put(Constant.GET_ALL_PRODUCTS_NAME, Constant.GetVal);

        ApiConfig.RequestToVolley((result, response) -> {
            if (result) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (!jsonObject.getBoolean(Constant.ERROR)) {
                        session.setData(Constant.GET_ALL_PRODUCTS_NAME, jsonObject.getString(Constant.DATA));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();

                }

            }
        }, activity, Constant.GET_ALL_PRODUCTS_URL, params, false);
    }

    @Override
    public void onPaymentSuccess(String razorpayPaymentID) {
        try {
            WalletTransactionFragment.payFromWallet = false;
            new WalletTransactionFragment().AddWalletBalance(activity, new Session(activity), WalletTransactionFragment.amount, WalletTransactionFragment.msg);

        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "onPaymentSuccess  ", e);
        }
    }

    @Override
    public void onPaymentError(int code, String response) {
        try {
            Toast.makeText(activity, getString(R.string.order_cancel), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "onPaymentError  ", e);
        }
    }


    @Override
    protected void onPause() {
        invalidateOptionsMenu();
        super.onPause();
    }

    @Override
    public void onTransactionResponse(Bundle bundle) {

    }

    @Override
    public void networkNotAvailable() {
        Toast.makeText(activity, "Network error", Toast.LENGTH_LONG).show();
    }

    @Override
    public void clientAuthenticationFailed(String s) {
        Toast.makeText(activity, s, Toast.LENGTH_LONG).show();
    }

    @Override
    public void someUIErrorOccurred(String s) {
        Toast.makeText(activity, s, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onErrorLoadingWebPage(int i, String s, String s1) {
        Toast.makeText(activity, s, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onBackPressedCancelTransaction() {
        Toast.makeText(activity, "Back Pressed", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onTransactionCancel(String s, Bundle bundle) {
        Toast.makeText(activity, s + bundle.toString(), Toast.LENGTH_LONG).show();
    }
}