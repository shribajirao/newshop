package wrteam.ecart.shop.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.common.api.Api;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import wrteam.ecart.shop.R;
import wrteam.ecart.shop.fragment.ProductDetailFragment;
import wrteam.ecart.shop.helper.ApiConfig;
import wrteam.ecart.shop.helper.Constant;
import wrteam.ecart.shop.helper.DatabaseHelper;
import wrteam.ecart.shop.helper.Session;
import wrteam.ecart.shop.model.Product;
import wrteam.ecart.shop.model.Variants;


@SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
public class ProductLoadMoreAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public final int VIEW_TYPE_ITEM = 0;
    public final int VIEW_TYPE_LOADING = 1;
    public final int resource;
    public final ArrayList<Product> productArrayList;
    final Activity activity;
    final Session session;
    final boolean isLogin;
    final DatabaseHelper databaseHelper;
    final String from;
    boolean isFavorite;

    public ProductLoadMoreAdapter(Activity activity, ArrayList<Product> myDataset, int resource, String from) {
        this.activity = activity;
        this.productArrayList = myDataset;
        this.resource = resource;
        this.from = from;
        this.session = new Session(activity);
        isLogin = session.getBoolean(Constant.IS_USER_LOGIN);
        Constant.CartValues = new HashMap<>();
        databaseHelper = new DatabaseHelper(activity);
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case (VIEW_TYPE_ITEM):
                view = LayoutInflater.from(activity).inflate(resource, parent, false);
                return new HolderItems(view);
            case (VIEW_TYPE_LOADING):
                view = LayoutInflater.from(activity).inflate(R.layout.item_progressbar, parent, false);
                return new ViewHolderLoading(view);
            default:
                throw new IllegalArgumentException("unexpected viewType: " + viewType);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holderParent, int position) {

        if (holderParent instanceof HolderItems) {
            HolderItems holder = (HolderItems) holderParent;
            holder.setIsRecyclable(false);
            try {
                Product product = productArrayList.get(position);

                ArrayList<Variants> variants = product.getVariants();
                if (variants.size() == 1) {
                    holder.spinner.setVisibility(View.INVISIBLE);
                    holder.lytSpinner.setVisibility(View.INVISIBLE);
                }
                if (!product.getIndicator().equals("0")) {
                    holder.imgIndicator.setVisibility(View.VISIBLE);
                    if (product.getIndicator().equals("1"))
                        holder.imgIndicator.setImageResource(R.drawable.ic_veg_icon);
                    else if (product.getIndicator().equals("2"))
                        holder.imgIndicator.setImageResource(R.drawable.ic_non_veg_icon);
                }
                holder.tvProductName.setText(Html.fromHtml(product.getName()));
                if (session.getData(Constant.ratings).equals("1")) {
                    holder.lytRatings.setVisibility(View.VISIBLE);
                    holder.tvRatingCount.setText("(" + product.getNumber_of_ratings() + ")");
                    holder.ratingProduct.setRating(Float.parseFloat(product.getRatings()));
                } else {
                    holder.lytRatings.setVisibility(View.GONE);
                }

                Picasso.get().
                        load(product.getImage())
                        .fit()
                        .centerInside()
                        .placeholder(R.drawable.placeholder)
                        .error(R.drawable.placeholder)
                        .into(holder.imgThumb);

                CustomAdapter customAdapter = new CustomAdapter(activity, variants, holder, product);
                holder.spinner.setAdapter(customAdapter);

                holder.lytMain.setOnClickListener(v -> {
                    if (Constant.CartValues.size() != 0)
                        ApiConfig.AddMultipleProductInCart(session, activity, Constant.CartValues);

                    AppCompatActivity activity1 = (AppCompatActivity) activity;
                    Fragment fragment = new ProductDetailFragment();
                    Bundle bundle = new Bundle();
                    bundle.putInt("variantsPosition", variants.size() == 1 ? 0 : holder.spinner.getSelectedItemPosition());
                    bundle.putString("id", product.getId());
                    bundle.putString(Constant.FROM, from);
                    bundle.putInt("position", position);
                    fragment.setArguments(bundle);
                    activity1.getSupportFragmentManager().beginTransaction().add(R.id.container, fragment).addToBackStack(null).commit();
                });


                if (isLogin) {
                    holder.tvQuantity.setText(variants.get(0).getCart_count());

                    if (product.getIs_favorite()) {
                        holder.imgFav.setImageResource(R.drawable.ic_is_favorite);
                    } else {
                        holder.imgFav.setImageResource(R.drawable.ic_is_not_favorite);
                    }
                    Session session = new Session(activity);

                    holder.imgFav.setOnClickListener(v -> {
                        try {
                            isFavorite = product.getIs_favorite();
                            if (from.equals("favorite")) {
                                isFavorite = false;
                                productArrayList.remove(product);
                                notifyDataSetChanged();
                            } else {
                                if (isFavorite) {
                                    isFavorite = false;
                                    holder.imgFav.setImageResource(R.drawable.ic_is_not_favorite);
                                    holder.lottieAnimationView.setVisibility(View.GONE);
                                } else {
                                    isFavorite = true;
                                    holder.lottieAnimationView.setVisibility(View.VISIBLE);
                                    holder.lottieAnimationView.playAnimation();
                                }
                                product.setIs_favorite(isFavorite);
                            }

                            ApiConfig.AddOrRemoveFavorite(activity, session, product.getVariants().get(0).getProduct_id(), isFavorite);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    });
                } else {

                    holder.tvQuantity.setText(databaseHelper.CheckCartItemExist(product.getVariants().get(0).getId(), product.getId()));

                    if (databaseHelper.getFavoriteById(product.getId())) {
                        holder.imgFav.setImageResource(R.drawable.ic_is_favorite);
                    } else {
                        holder.imgFav.setImageResource(R.drawable.ic_is_not_favorite);
                    }

                    holder.imgFav.setOnClickListener(v -> {
                        isFavorite = databaseHelper.getFavoriteById(product.getId());
                        if (from.equals("favorite")) {
                            isFavorite = false;
                            productArrayList.remove(product);
                            notifyDataSetChanged();
                        } else {
                            if (isFavorite) {
                                isFavorite = false;
                                holder.imgFav.setImageResource(R.drawable.ic_is_not_favorite);
                                holder.lottieAnimationView.setVisibility(View.GONE);
                            } else {
                                isFavorite = true;
                                holder.lottieAnimationView.setVisibility(View.VISIBLE);
                                holder.lottieAnimationView.playAnimation();
                            }
                        }
                        databaseHelper.AddOrRemoveFavorite(product.getVariants().get(0).getProduct_id(), isFavorite);
                    });
                }

                SetSelectedData(holder, variants.get(0), product);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (holderParent instanceof ViewHolderLoading) {
            ViewHolderLoading loadingViewHolder = (ViewHolderLoading) holderParent;
            loadingViewHolder.progressBar.setIndeterminate(true);
        }

    }

    @Override
    public int getItemCount() {
        return productArrayList.size();
    }


    @Override
    public int getItemViewType(int position) {
        return productArrayList.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    @Override
    public long getItemId(int position) {
        Product product = productArrayList.get(position);
        if (product != null)
            return Integer.parseInt(product.getId());
        else
            return position;
    }

    @SuppressLint("SetTextI18n")
    public void SetSelectedData(HolderItems holder, Variants variants, Product product) {

//        GST_Amount (Original Cost x GST %)/100
//        Net_Price Original Cost + GST Amount

        holder.tvMeasurement.setText(variants.getMeasurement() + variants.getMeasurement_unit_name());

        if (session.getBoolean(Constant.IS_USER_LOGIN)) {
            if (Constant.CartValues.containsKey(variants.getId())) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    holder.tvQuantity.setText("" + Constant.CartValues.get(variants.getId()));
                }
            }
        } else {
            if (session.getData(variants.getId()) != null) {
                holder.tvQuantity.setText(session.getData(variants.getId()));
            } else {
                holder.tvQuantity.setText(variants.getCart_count());
            }
        }

        double OriginalPrice = 0, DiscountedPrice = 0;
        String taxPercentage = "0";
        try {
            taxPercentage = (Double.parseDouble(product.getTax_percentage()) > 0 ? product.getTax_percentage() : "0");
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (variants.getIs_flash_sales().equals("false")) {
            DiscountedPrice = ((Float.parseFloat(variants.getDiscounted_price()) + ((Float.parseFloat(variants.getDiscounted_price()) * Float.parseFloat(taxPercentage)) / 100)));
            OriginalPrice = (Float.parseFloat(variants.getPrice()) + ((Float.parseFloat(variants.getPrice()) * Float.parseFloat(taxPercentage)) / 100));

            if (variants.getDiscounted_price().equals("0") || variants.getDiscounted_price().equals("")) {
                holder.showDiscount.setVisibility(View.GONE);
                holder.tvPrice.setText(session.getData(Constant.currency) + ApiConfig.StringFormat("" + OriginalPrice));
            } else {
                holder.showDiscount.setVisibility(View.VISIBLE);
                holder.tvOriginalPrice.setPaintFlags(holder.tvOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                holder.tvOriginalPrice.setText(session.getData(Constant.currency) + ApiConfig.StringFormat("" + OriginalPrice));
                holder.tvPrice.setText(session.getData(Constant.currency) + ApiConfig.StringFormat("" + DiscountedPrice));
            }
        } else {
            holder.lytTimer.setVisibility(View.VISIBLE);
            String strCurrentDate = session.getData(Constant.current_date);
            String strStartDate = product.getVariants().get(0).getFlash_sales().get(0).getStart_date().split("\\s")[0];
            String strEndDate = product.getVariants().get(0).getFlash_sales().get(0).getEnd_date().split("\\s")[0];

            long timeDiff = ApiConfig.dayBetween(strCurrentDate, strStartDate);

            if (timeDiff < 0) {
                timeDiff = (ApiConfig.dayBetween(strCurrentDate, strEndDate) * (-1));
            }

            if (timeDiff < 0) {
                holder.tvTimerTitle.setText(activity.getString(R.string.ends_in));
                holder.tvTimer.setText((timeDiff * (-1)) + activity.getString(R.string.day));
                if (product.getVariants().get(0).getFlash_sales().get(0).getDiscounted_price().equals("0") || product.getVariants().get(0).getFlash_sales().get(0).getDiscounted_price().equals("")) {
                    holder.showDiscount.setVisibility(View.GONE);
                    holder.tvPrice.setText(session.getData(Constant.currency) + ApiConfig.StringFormat("" + OriginalPrice));
                } else {
                    holder.showDiscount.setVisibility(View.VISIBLE);
                    DiscountedPrice = ((Float.parseFloat(product.getVariants().get(0).getFlash_sales().get(0).getDiscounted_price()) + ((Float.parseFloat(product.getVariants().get(0).getFlash_sales().get(0).getDiscounted_price()) * Float.parseFloat(taxPercentage)) / 100)));
                    OriginalPrice = (Float.parseFloat(product.getVariants().get(0).getFlash_sales().get(0).getPrice()) + ((Float.parseFloat(product.getVariants().get(0).getFlash_sales().get(0).getPrice()) * Float.parseFloat(taxPercentage)) / 100));
                    holder.tvOriginalPrice.setPaintFlags(holder.tvOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    holder.tvOriginalPrice.setText(session.getData(Constant.currency) + ApiConfig.StringFormat("" + OriginalPrice));
                    holder.tvPrice.setText(session.getData(Constant.currency) + ApiConfig.StringFormat("" + DiscountedPrice));
                }
            } else if (timeDiff > 0) {
                holder.tvTimerTitle.setText(activity.getString(R.string.starts_in));
                holder.tvTimer.setText(timeDiff + activity.getString(R.string.day));
                if (product.getVariants().get(0).getDiscounted_price().equals("0") || product.getVariants().get(0).getDiscounted_price().equals("")) {
                    holder.showDiscount.setVisibility(View.GONE);
                    holder.tvPrice.setText(session.getData(Constant.currency) + ApiConfig.StringFormat("" + OriginalPrice));
                } else {
                    holder.showDiscount.setVisibility(View.VISIBLE);
                    DiscountedPrice = ((Float.parseFloat(product.getVariants().get(0).getDiscounted_price()) + ((Float.parseFloat(product.getVariants().get(0).getDiscounted_price()) * Float.parseFloat(taxPercentage)) / 100)));
                    OriginalPrice = (Float.parseFloat(product.getVariants().get(0).getPrice()) + ((Float.parseFloat(product.getVariants().get(0).getPrice()) * Float.parseFloat(taxPercentage)) / 100));
                    holder.tvOriginalPrice.setPaintFlags(holder.tvOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    holder.tvOriginalPrice.setText(session.getData(Constant.currency) + ApiConfig.StringFormat("" + OriginalPrice));
                    holder.tvPrice.setText(session.getData(Constant.currency) + ApiConfig.StringFormat("" + DiscountedPrice));
                }
            } else {
                holder.tvTimerTitle.setText(activity.getString(R.string.ends_in));
                StartTimer(holder, product.getVariants().get(0));
                if (product.getVariants().get(0).getFlash_sales().get(0).getDiscounted_price().equals("0") || product.getVariants().get(0).getFlash_sales().get(0).getDiscounted_price().equals("")) {
                    holder.showDiscount.setVisibility(View.GONE);
                    holder.tvPrice.setText(session.getData(Constant.currency) + ApiConfig.StringFormat("" + OriginalPrice));
                } else {
                    holder.showDiscount.setVisibility(View.VISIBLE);
                    DiscountedPrice = ((Float.parseFloat(product.getVariants().get(0).getFlash_sales().get(0).getDiscounted_price()) + ((Float.parseFloat(product.getVariants().get(0).getFlash_sales().get(0).getDiscounted_price()) * Float.parseFloat(taxPercentage)) / 100)));
                    OriginalPrice = (Float.parseFloat(product.getVariants().get(0).getFlash_sales().get(0).getPrice()) + ((Float.parseFloat(product.getVariants().get(0).getFlash_sales().get(0).getPrice()) * Float.parseFloat(taxPercentage)) / 100));
                    holder.tvOriginalPrice.setPaintFlags(holder.tvOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    holder.tvOriginalPrice.setText(session.getData(Constant.currency) + ApiConfig.StringFormat("" + OriginalPrice));
                    holder.tvPrice.setText(session.getData(Constant.currency) + ApiConfig.StringFormat("" + DiscountedPrice));
                }
            }

            holder.showDiscount.setText("-" + ApiConfig.GetDiscount(OriginalPrice, DiscountedPrice));
        }

        holder.showDiscount.setText("-" + ApiConfig.GetDiscount(OriginalPrice, DiscountedPrice));

        if (variants.getServe_for().equalsIgnoreCase(Constant.SOLD_OUT_TEXT)) {
            holder.tvStatus.setVisibility(View.VISIBLE);
            holder.lytQuantity.setVisibility(View.GONE);
        } else {
            holder.tvStatus.setVisibility(View.GONE);
            holder.lytQuantity.setVisibility(View.VISIBLE);
        }

        if (isLogin) {
            if (Constant.CartValues.containsKey(variants.getId())) {
                holder.tvQuantity.setText("" + Constant.CartValues.get(variants.getId()));
            } else {
                holder.tvQuantity.setText(variants.getCart_count());
            }

            if (variants.getCart_count().equals("0")) {
                holder.btnAddToCart.setVisibility(View.VISIBLE);
            } else {
                if (session.getData(Constant.STATUS).equals("1")) {
                    holder.btnAddToCart.setVisibility(View.GONE);
                } else {
                    holder.btnAddToCart.setVisibility(View.VISIBLE);
                }
            }
        } else {
            if (databaseHelper.CheckCartItemExist(variants.getId(), variants.getId()).equals("0")) {
                holder.btnAddToCart.setVisibility(View.VISIBLE);
            } else {
                holder.btnAddToCart.setVisibility(View.GONE);
            }

            holder.tvQuantity.setText(databaseHelper.CheckCartItemExist(variants.getId(), variants.getId()));
        }

        holder.btnMinusQty.setOnClickListener(view -> removeFromCartClickEvent(holder, variants));

        holder.btnAddQty.setOnClickListener(view -> addToCartClickEvent(holder, variants));

        holder.btnAddToCart.setOnClickListener(v -> addToCartClickEvent(holder, variants));
    }

    public void addToCartClickEvent(HolderItems holder, Variants variants) {
        if (session.getData(Constant.STATUS).equals("1")) {
            int count = Integer.parseInt(holder.tvQuantity.getText().toString());
            if (count < Float.parseFloat(variants.getStock())) {
                if (count < Integer.parseInt(session.getData(Constant.max_cart_items_count))) {
                    if (count == 0) {
                        holder.btnAddToCart.setVisibility(View.GONE);
                    }
                    count++;
                    holder.tvQuantity.setText("" + count);
                    if (isLogin) {
                        if (Constant.CartValues.containsKey(variants.getId())) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                Constant.CartValues.replace(variants.getId(), "" + count);
                            }
                        } else {
                            Constant.CartValues.put(variants.getId(), "" + count);
                        }
                        ApiConfig.AddMultipleProductInCart(session, activity, Constant.CartValues);
                    } else {
                        holder.tvQuantity.setText("" + count);
                        databaseHelper.AddToCart(variants.getId(), variants.getProduct_id(), "" + count);
                        databaseHelper.getTotalItemOfCart(activity);
                    }
                } else {
                    Toast.makeText(activity, activity.getString(R.string.limit_alert), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(activity, activity.getString(R.string.stock_limit), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(activity, activity.getString(R.string.user_deactivate_msg), Toast.LENGTH_SHORT).show();
        }
    }

    public void removeFromCartClickEvent(HolderItems holder, Variants variants) {
        if (session.getData(Constant.STATUS).equals("1")) {
            int count = Integer.parseInt(holder.tvQuantity.getText().toString());
            if (count < Float.parseFloat(variants.getStock())) {
                if (count < Integer.parseInt(session.getData(Constant.max_cart_items_count))) {
                    count--;
                    if (count == 0) {
                        holder.btnAddToCart.setVisibility(View.VISIBLE);
                    }
                    holder.tvQuantity.setText("" + count);
                    if (isLogin) {
                        if (count <= 0) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                holder.tvQuantity.setText("" + count);
                                if (Constant.CartValues.containsKey(variants.getId())) {
                                    Constant.CartValues.replace(variants.getId(), "" + count);
                                } else {
                                    Constant.CartValues.put(variants.getId(), "" + count);
                                }
                            }
                            ApiConfig.AddMultipleProductInCart(session, activity, Constant.CartValues);
                        }
                    } else {
                        holder.tvQuantity.setText("" + count);
                        databaseHelper.AddToCart(variants.getId(), variants.getProduct_id(), "" + count);
                        databaseHelper.getTotalItemOfCart(activity);
                    }
                } else {
                    Toast.makeText(activity, activity.getString(R.string.limit_alert), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(activity, activity.getString(R.string.stock_limit), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(activity, activity.getString(R.string.user_deactivate_msg), Toast.LENGTH_SHORT).show();
        }
    }

    static class ViewHolderLoading extends RecyclerView.ViewHolder {
        public final ProgressBar progressBar;

        public ViewHolderLoading(View view) {
            super(view);
            progressBar = view.findViewById(R.id.itemProgressbar);
        }
    }

    public static class HolderItems extends RecyclerView.ViewHolder {
        public final ImageButton btnAddQty;
        public final ImageButton btnMinusQty;
        final TextView tvProductName;
        final TextView tvPrice;
        final TextView tvQuantity;
        final TextView tvMeasurement;
        final TextView showDiscount;
        final TextView tvTimerTitle;
        final TextView tvOriginalPrice;
        final TextView tvStatus;
        final ImageView imgThumb;
        final ImageView imgFav;
        final ImageView imgIndicator;
        final RelativeLayout lytSpinner;
        final CardView lytMain;
        final AppCompatSpinner spinner;
        final RelativeLayout lytQuantity, lytTimer;
        final LottieAnimationView lottieAnimationView;
        final Button btnAddToCart;
        final RatingBar ratingProduct;
        final TextView tvRatingCount;
        final LinearLayout lytRatings;
        final TextView tvTimer;

        public HolderItems(View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            showDiscount = itemView.findViewById(R.id.showDiscount);
            tvOriginalPrice = itemView.findViewById(R.id.tvOriginalPrice);
            tvTimerTitle = itemView.findViewById(R.id.tvTimerTitle);
            tvMeasurement = itemView.findViewById(R.id.tvMeasurement);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            imgThumb = itemView.findViewById(R.id.imgThumb);
            imgIndicator = itemView.findViewById(R.id.imgIndicator);
            btnAddQty = itemView.findViewById(R.id.btnAddQty);
            btnMinusQty = itemView.findViewById(R.id.btnMinusQty);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            lytQuantity = itemView.findViewById(R.id.lytQuantity);
            lytTimer = itemView.findViewById(R.id.lytTimer);
            imgFav = itemView.findViewById(R.id.imgFav);
            lytMain = itemView.findViewById(R.id.lytMain);
            spinner = itemView.findViewById(R.id.spinner);
            lytSpinner = itemView.findViewById(R.id.lytSpinner);
            btnAddToCart = itemView.findViewById(R.id.btnAddToCart);
            ratingProduct = itemView.findViewById(R.id.ratingProduct);
            tvRatingCount = itemView.findViewById(R.id.tvRatingCount);
            lytRatings = itemView.findViewById(R.id.lytRatings);
            lottieAnimationView = itemView.findViewById(R.id.lottieAnimationView);
            tvTimer = itemView.findViewById(R.id.tvTimer);

        }

    }

    public class CustomAdapter extends BaseAdapter {
        final Context context;
        final ArrayList<Variants> extraList;
        final LayoutInflater inflter;
        final HolderItems holder;
        final Product product;

        public CustomAdapter(Context applicationContext, ArrayList<Variants> extraList, HolderItems holder, Product product) {
            this.context = applicationContext;
            this.extraList = extraList;
            this.holder = holder;
            this.product = product;
            inflter = (LayoutInflater.from(applicationContext));
        }

        @Override
        public int getCount() {
            return extraList.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @SuppressLint({"SetTextI18n", "ViewHolder", "InflateParams"})
        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            view = inflter.inflate(R.layout.lyt_spinner_item, null);
            TextView measurement = view.findViewById(R.id.tvMeasurement);


            Variants variants = extraList.get(i);
            measurement.setText(variants.getMeasurement() + " " + variants.getMeasurement_unit_name());

            if (variants.getServe_for().equalsIgnoreCase(Constant.SOLD_OUT_TEXT)) {
                measurement.setTextColor(ContextCompat.getColor(context, R.color.red));
            } else {
                measurement.setTextColor(ContextCompat.getColor(context, R.color.black));
            }

            holder.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    Variants variants = extraList.get(i);
                    SetSelectedData(holder, variants, product);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });

            return view;
        }
    }

    @SuppressLint({"DefaultLocale", "SetTextI18n", "SimpleDateFormat"})
    public void StartTimer(HolderItems itemHolder, Variants variants) {
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            formatter.setLenient(false);

            String endTime = variants.getFlash_sales().get(0).getEnd_date();
            long milliseconds = 0;

            Date endDate;
            try {
                endDate = formatter.parse(endTime);
                assert endDate != null;
                milliseconds = endDate.getTime();

            } catch (ParseException e) {
                e.printStackTrace();
            }

            new CountDownTimer(milliseconds, 1000) {
                @SuppressWarnings("deprecation")
                @Override
                public void onTick(long millisUntilFinished) {
                    String hoursLeft = String.format(((millisUntilFinished / (1000 * 60 * 60)) % 24) > 9 ? "%d" : "%02d", (millisUntilFinished / (1000 * 60 * 60)) % 24);
                    String minutesLeft = String.format(((millisUntilFinished / (1000 * 60)) % 60) > 9 ? "%d" : "%02d", (millisUntilFinished / (1000 * 60)) % 60);
                    String secondsLeft = String.format(((millisUntilFinished / 1000) % 60) > 9 ? "%d" : "%02d", (millisUntilFinished / 1000) % 60);

                    if ((Integer.parseInt(hoursLeft) >= 0 && Integer.parseInt(minutesLeft) >= 0 && Integer.parseInt(secondsLeft) >= 0)) {
                        itemHolder.tvTimer.setText(hoursLeft + ":" + minutesLeft + ":" + secondsLeft);
                    } else {
                        variants.setIs_flash_sales("false");
                        notifyItemChanged(itemHolder.getPosition());
                    }
                }

                @Override
                public void onFinish() {
                }
            }.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
