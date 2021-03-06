package in.kay.edvora.Adapter;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Pair;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.github.thunder413.datetimeutils.DateTimeUnits;
import com.github.thunder413.datetimeutils.DateTimeUtils;
import com.pixplicity.easyprefs.library.Prefs;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import in.kay.edvora.Api.ApiInterface;
import in.kay.edvora.Application.MyApplication;
import in.kay.edvora.Models.HomeModel;
import in.kay.edvora.Models.Id;
import in.kay.edvora.Models.PostedBy;
import in.kay.edvora.R;
import in.kay.edvora.Utils.CustomToast;
import in.kay.edvora.Views.Activity.AnswerActivity;
import in.kay.edvora.Views.Activity.Profile;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HomeFeedAdapter extends RecyclerView.Adapter<HomeFeedAdapter.ViewHolder> {
    List<HomeModel> list;
    Context context;

    public HomeFeedAdapter(List<HomeModel> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.post_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        PostedBy pb = list.get(position).getPostedBy();
        Id id = pb.getId();
        final String username = id.getName();
        final String userID = id.get_id();
        final String userimage = id.getImageUrl();

        String strDate = list.get(position).getCreatedAt();
        Date postDate = GetDate(strDate);
        final Integer difference = GetDateDiff(postDate);
        if (difference == 0) {
            holder.tvDate.setText("Asked Today");
        } else if (difference == 1) {
            holder.tvDate.setText("Asked Yesterday");
        } else if (difference > 356) {
            holder.tvDate.setText("Asked a long time ago");
        } else {
            holder.tvDate.setText(difference + " days ago");
        }
        if (TextUtils.isEmpty(list.get(position).getSubject()) && TextUtils.isEmpty(list.get(position).getTopic())) {
            holder.tvTopic.setVisibility(View.GONE);
        } else if (TextUtils.isEmpty(list.get(position).getTopic())) {
            holder.tvTopic.setText(list.get(position).getSubject());
        } else if (TextUtils.isEmpty(list.get(position).getSubject())) {
            holder.tvTopic.setText(list.get(position).getTopic());
        } else {
            holder.tvTopic.setText(list.get(position).getSubject() + " ● " + list.get(position).getTopic());
        }
        holder.tvQuestion.setText(list.get(position).getQuestion());
        holder.tvName.setText(username);
        if (!TextUtils.isEmpty(list.get(position).getImageUrl())) {
            holder.cardView.setVisibility(View.VISIBLE);
            Picasso.get()
                    .load(list.get(position).getImageUrl())
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .into(holder.iv_postimg, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError(Exception e) {
                            Picasso.get()
                                    .load(list.get(position).getImageUrl())
                                    .placeholder(R.drawable.img_place_holder)
                                    .error(R.drawable.img_place_holder)
                                    .into(holder.iv_postimg);
                        }
                    });
        } else holder.cardView.setVisibility(View.GONE);
        if (!TextUtils.isEmpty(userimage)) {
            Picasso.get()
                    .load(userimage)
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .into(holder.circleImageView, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError(Exception e) {
                            Picasso.get()
                                    .load(userimage)
                                    .placeholder(R.drawable.img_place_holder)
                                    .error(R.drawable.img_place_holder)
                                    .into(holder.circleImageView);
                        }
                    });
        }

        if (Prefs.getString("userId", "").equalsIgnoreCase(userID) && difference < 2)
            holder.ivMore.setVisibility(View.VISIBLE);
        else if (!Prefs.getString("userId", "").equalsIgnoreCase(userID))
            holder.ivMore.setVisibility(View.VISIBLE);
        else
            holder.ivMore.setVisibility(View.GONE);


        holder.ivMore.setOnClickListener(view -> {
            Context wrapper = new ContextThemeWrapper(context, R.style.PopupMenu);
            PopupMenu popupMenu = new PopupMenu(wrapper, view);
            if (Prefs.getString("userId", "").equalsIgnoreCase(userID)) {
                popupMenu.inflate(R.menu.popup_menu);
            } else
                popupMenu.inflate(R.menu.popup_menu_normal);
            popupMenu.setOnMenuItemClickListener(menuItem -> {
                switch (menuItem.getItemId()) {
                    case R.id.edit:
                        Toast.makeText(context, "Edit button clicked", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.delete:
                        Toast.makeText(context, "Delete button clicked", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.report:
                        Toast.makeText(context, "Report button clicked", Toast.LENGTH_SHORT).show();
                        break;
                }
                return true;
            });
            popupMenu.show();
        });
        holder.itemView.setOnClickListener(view -> {
            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) context, holder.cl, ViewCompat.getTransitionName(holder.cl));
            Intent intent = new Intent(context, AnswerActivity.class);
            intent.putExtra("question", list.get(position).getQuestion());
            intent.putExtra("days", Integer.toString(difference));
            intent.putExtra("isFirstTime", true);
            intent.putExtra("postID", list.get(position).get_id());
            intent.putExtra("name", list.get(position).getPostedBy().getId().getName());
            intent.putExtra("topic", list.get(position).getSubject() + " ● " + list.get(position).getTopic());
            if (list.get(position).getImageUrl() != null) {
                intent.putExtra("postimageUrl", list.get(position).getImageUrl());
            } else {
                intent.putExtra("postimageUrl", "");
            }
            if (!TextUtils.isEmpty(userimage)) {
                intent.putExtra("profileimageUrl", userimage);
            } else {
                intent.putExtra("profileimageUrl", "");
            }
            intent.putExtra("userId", userID);
            context.startActivity(intent, options.toBundle());
        });
        holder.llBookmark.setOnClickListener(view -> {
            CustomToast customToast = new CustomToast();
            customToast.ShowToast(context, "Bookmark is clicked");
        });
        holder.llShare.setOnClickListener(view -> {
            Intent txtIntent = new Intent(Intent.ACTION_SEND);
            txtIntent.setType("text/plain");
            txtIntent.putExtra(Intent.EXTRA_SUBJECT, "Edvora");
            txtIntent.putExtra(Intent.EXTRA_TEXT, list.get(position).getQuestion().substring(0, Math.min(list.get(position).getQuestion().length(), 160)) + "..." + "\n" + "-by  " + list.get(position).getPostedBy().getId().getName() + "\n" + "https://www.edvora.in/post/" + list.get(position).get_id());
            context.startActivity(Intent.createChooser(txtIntent, "Share"));
        });
        holder.circleImageView.setOnClickListener(view -> {
            View image = holder.circleImageView;
            View text = holder.tvName;
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation((Activity) context, Pair.create(image, "Profile"), Pair.create(text, "Name"));
            Intent intent = new Intent(context, Profile.class);
            intent.putExtra("userId", userID);
            context.startActivity(intent, options.toBundle());
        });
        holder.llBookmark.setOnClickListener(view -> {
            DoBookMark(list.get(position).get_id());
        });
    }

    private void DoBookMark(String id) {
        final ProgressDialog pd = new ProgressDialog(context);
        pd.setMax(100);
        pd.setMessage("Adding post to saved Post...");
        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pd.show();
        pd.setCancelable(false);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ApiInterface.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        ApiInterface apiInterface = retrofit.create(ApiInterface.class);
        Call<ResponseBody> call = apiInterface.savePost(id,"Bearer "+Prefs.getString("accessToken",""));
        call.enqueue(new retrofit2.Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful())
                {
                    pd.dismiss();
                    CustomToast customToast=new CustomToast();
                    customToast.ShowToast(context,"Added to saved posts...");
                }else if (response.code()==502)
                {
                    MyApplication myApplication = new MyApplication();
                    myApplication.RefreshToken(Prefs.getString("refreshToken", ""), context);
                    DoBookMark(id);
                }else {
                    pd.dismiss();
                    CustomToast customToast=new CustomToast();
                    customToast.ShowToast(context,"Already added to your saved post..");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                pd.dismiss();
                CustomToast customToast=new CustomToast();
                customToast.ShowToast(context,"Failure "+t.getLocalizedMessage());
            }
        });
    }

    public int GetDateDiff(Date date) {
        Date currentDate = new Date();
        Date postDate = date;
        int diff = DateTimeUtils.getDateDiff(currentDate, postDate, DateTimeUnits.DAYS);
        return diff;
    }

    public Date GetDate(String string) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        try {
            Date date = format.parse(string);
            return date;
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView circleImageView;
        TextView tvName, tvTopic, tvQuestion, tvDate;
        CardView cardView;
        LinearLayout llAnswer, llBookmark, llShare;
        ImageView iv_postimg, ivMore;
        ConstraintLayout cl;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            circleImageView = itemView.findViewById(R.id.iv_profile);
            tvName = itemView.findViewById(R.id.tvName);
            cl = itemView.findViewById(R.id.cl);
            tvTopic = itemView.findViewById(R.id.tvTopic);
            tvDate = itemView.findViewById(R.id.tvDays);
            tvQuestion = itemView.findViewById(R.id.tvQuestion);
            iv_postimg = itemView.findViewById(R.id.iv_postimg);
            ivMore = itemView.findViewById(R.id.iv_more);
            cardView = itemView.findViewById(R.id.cardView);
            llAnswer = itemView.findViewById(R.id.llAnswer);
            llBookmark = itemView.findViewById(R.id.llBookmark);
            llShare = itemView.findViewById(R.id.llShare);
            cardView.setVisibility(View.GONE);
        }
    }
}
