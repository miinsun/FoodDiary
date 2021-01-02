package ddwucom.mobile.ma01_20180986;

import android.content.Context;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MyCursorAdapter extends CursorAdapter {

    LayoutInflater inflater;
    int layout;

    public MyCursorAdapter(Context context, int layout, Cursor c) {
        super(context, c, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.layout = layout;
    }

    @Override

    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = inflater.inflate(layout, parent, false);
        ViewHolder holder = new ViewHolder();
        view.setTag(holder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder)view.getTag();

        if(holder.tvContactName == null){
            holder.tvContactName= view.findViewById(R.id.tvContactName);
            holder.tvContactLocation = view.findViewById(R.id.tvContactLocation);
            holder.tvContactReview = view.findViewById(R.id.tvContactReview);
            holder.imageView = view.findViewById(R.id.contactImage);
        }

//        TextView tvContactName = view.findViewById(R.id.tvContactName);
//        TextView tvContactPhone = view.findViewById(R.id.tvContactPhone);

        holder.tvContactName.setText(cursor.getString(cursor.getColumnIndex(ContactDBHelper.COL_NAME)));
        holder.tvContactReview.setText(cursor.getString(cursor.getColumnIndex(ContactDBHelper.COL_REVIEW)));
        holder.tvContactLocation.setText(cursor.getString(cursor.getColumnIndex(ContactDBHelper.COL_LOCATION)));

        String image = cursor.getString(cursor.getColumnIndex(ContactDBHelper.COL_PHOTO));
        if(image != null) {
            holder.imageView.setImageBitmap(BitmapFactory.decodeFile(cursor.getString(cursor.getColumnIndex(ContactDBHelper.COL_PHOTO))));
        }
        else {
            Drawable drawable = context.getResources().getDrawable(R.drawable.defaultimage);
            holder.imageView.setImageDrawable(drawable);
        }

    }

    //뷰 홀더 생성
    static class ViewHolder{

        public ViewHolder(){
            tvContactName = null;
            tvContactReview = null;
            tvContactLocation = null;
            imageView = null;
        }
        TextView tvContactName;
        TextView tvContactReview;
        TextView tvContactLocation;
        ImageView imageView;

    }
}