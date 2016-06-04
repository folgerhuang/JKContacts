package com.jikexueyuan.jkcontacts;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.ContactsContract;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by shiyu on 6/2/2016.
 */
public class ContactsOp {
    public static List<ContactBean> getContacts(Context context) {
        List<ContactBean> contacts = new ArrayList<ContactBean>();

        ContentResolver resolver = context.getContentResolver();
        Cursor cRawContact = resolver.query(ContactsContract.RawContacts.CONTENT_URI,
                new String[] { ContactsContract.RawContacts._ID }, null, null, null);

        ContactBean contact;
        while (cRawContact.moveToNext()) {
            contact = new ContactBean();

            long rawContactId = cRawContact.getLong(cRawContact
                    .getColumnIndex(ContactsContract.RawContacts._ID));
            contact.setRawContactId(rawContactId);

            Cursor dataCursor = resolver.query(ContactsContract.Data.CONTENT_URI, null,
                    ContactsContract.Contacts.Data.RAW_CONTACT_ID + "=?",
                    new String[] { String.valueOf(rawContactId) }, null);

            while (dataCursor.moveToNext()) {
                String data1 = dataCursor.getString(dataCursor
                        .getColumnIndex(ContactsContract.Contacts.Data.DATA1));
                String mimetype = dataCursor.getString(dataCursor
                        .getColumnIndex(ContactsContract.Contacts.Data.MIMETYPE));

                if (mimetype.equals(ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)) {
                    contact.setName(data1);
                } else if (mimetype.equals(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)) {
                    contact.setPhone(data1);
                }
            }
            dataCursor.close();
            contacts.add(contact);
        }



        cRawContact.close();
        return contacts;
    }

    public static void addContact(Context context, ContactBean contact) {
        ContentResolver resolver = context.getContentResolver();

        ContentValues values = new ContentValues();
        Uri rawContactUri = resolver.insert(ContactsContract.RawContacts.CONTENT_URI, values);
        long rawContactId = ContentUris.parseId(rawContactUri);

        ContentValues valuesData1 = new ContentValues();
        valuesData1.put(ContactsContract.Contacts.Data.RAW_CONTACT_ID, rawContactId);
        valuesData1.put(ContactsContract.Contacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
        valuesData1.put(ContactsContract.CommonDataKinds.Phone.NUMBER, contact.getPhone());
        resolver.insert(ContactsContract.Data.CONTENT_URI, valuesData1);

        ContentValues valuesData2 = new ContentValues();
        valuesData2.put(ContactsContract.Contacts.Data.RAW_CONTACT_ID, rawContactId);
        valuesData2.put(ContactsContract.Contacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
        valuesData2.put(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, contact.getName());
        resolver.insert(ContactsContract.Data.CONTENT_URI, valuesData2);

    }

    public static void updateContact(Context context, ContactBean contact) {
        ContentResolver resolver = context.getContentResolver();

        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

        ops.add(ContentProviderOperation
                .newUpdate(ContactsContract.Data.CONTENT_URI)
                .withSelection(
                        ContactsContract.Contacts.Data.RAW_CONTACT_ID + "=? AND " + ContactsContract.Contacts.Data.MIMETYPE + "=?",
                        new String[] {
                                String.valueOf(contact.getRawContactId()),
                                ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE })
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, contact.getName())
                .build());

        ops.add(ContentProviderOperation
                .newUpdate(ContactsContract.Data.CONTENT_URI)
                .withSelection(
                        ContactsContract.Contacts.Data.RAW_CONTACT_ID + "=? AND " + ContactsContract.Contacts.Data.MIMETYPE + "=?",
                        new String[] {
                                String.valueOf(contact.getRawContactId()),
                                ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE })
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, contact.getPhone()).build());
        try {
            resolver.applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        }

    }

    public static void deleteContact(Context context, ContactBean contact) {
        ContentResolver resolver = context.getContentResolver();
        resolver.delete(ContactsContract.RawContacts.CONTENT_URI, ContactsContract.RawContacts._ID + "=?",
                new String[] { String.valueOf(contact.getRawContactId()) });
    }
}
