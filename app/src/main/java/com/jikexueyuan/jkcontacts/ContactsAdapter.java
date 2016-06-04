package com.jikexueyuan.jkcontacts;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by shiyu on 6/2/2016.
 */
public class ContactsAdapter extends BaseAdapter {
    private Context context;
    private Activity activity;
    private List<ContactBean> lists;
    private LinearLayout layout;

    public ContactsAdapter(Context context, Activity activity,List<ContactBean> lists) {
        this.context = context;
        this.activity = activity;
        this.lists =lists ;
    }

    public List<ContactBean> getContacts() {

        List<ContactBean> contacts = new ArrayList<ContactBean>();

        ContentResolver resolver = context.getContentResolver();
        Cursor cRawContact = resolver.query(ContactsContract.RawContacts.CONTENT_URI,
                new String[]{ContactsContract.RawContacts._ID}, null, null, null);

        ContactBean contact;
        while (cRawContact.moveToNext()) {
            contact = new ContactBean();

            long rawContactId = cRawContact.getLong(cRawContact
                    .getColumnIndex(ContactsContract.RawContacts._ID));
            contact.setRawContactId(rawContactId);

            Cursor dataCursor = resolver.query(ContactsContract.Data.CONTENT_URI, null,
                    ContactsContract.Data.RAW_CONTACT_ID + "=?",
                    new String[]{String.valueOf(rawContactId)}, null);

            while (dataCursor.moveToNext()) {
                String data1 = dataCursor.getString(dataCursor
                        .getColumnIndex(ContactsContract.Data.DATA1));
                String mimetype = dataCursor.getString(dataCursor
                        .getColumnIndex(ContactsContract.Data.MIMETYPE));

                if (mimetype.equals(ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)) {
                    contact.setName(data1);
                } else if (mimetype.equals(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)) {
                    contact.setPhone(data1);
                }
            }
            contacts.add(contact);
            dataCursor.close();
        }

        cRawContact.close();


        return contacts;

    }

    @Override
    public int getCount() {
        return lists.size();
    }

    @Override
    public Object getItem(int position) {
        return lists.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      /*  LayoutInflater inflater = LayoutInflater.from(context);
        layout = (LinearLayout) inflater.inflate(R.layout.user, null);
        TextView tvName= (TextView) layout.findViewById(R.id.name);
        TextView tvPhone=(TextView)layout.findViewById(R.id.phone);

        tvName.setText(lists.get(position).getName());
        tvPhone.setText(lists.get(position).getPhone());*/
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.user, null);
            holder = new ViewHolder();
            holder.tvName = (TextView) convertView.findViewById(R.id.name);
            holder.tvPhone = (TextView) convertView.findViewById(R.id.phone);
            holder.tvName.setText(lists.get(position).getName());
            holder.tvPhone.setText(lists.get(position).getPhone());
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
            holder.tvName.setText(lists.get(position).getName());
            holder.tvPhone.setText(lists.get(position).getPhone());
        }
        return convertView;
    }

    private static class ViewHolder {
        TextView tvName;
        TextView tvPhone;
    }
}
