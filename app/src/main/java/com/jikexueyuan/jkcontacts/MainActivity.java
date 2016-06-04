package com.jikexueyuan.jkcontacts;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int REQUESTCODE = 123;
    private Button btnAddUser;
    private ListView lvContacts;
    private List<ContactBean> listContacts;

    private ContactsAdapter myAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lvContacts = (ListView) findViewById(R.id.lvListContacts);

        if (GetPermission(this, this, Manifest.permission.READ_CONTACTS)) {
            listContacts = ContactsOp.getContacts(this);
            myAdapter = new ContactsAdapter(this, this, listContacts);
            lvContacts.setAdapter(myAdapter);
            lvContacts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    //刷新listContacts，防止小标越界
                    listContacts = ContactsOp.getContacts(MainActivity.this);
                    ContactBean contactBean = listContacts.get(position);
                    //Toast.makeText(MainActivity.this,contactBean.toString(),Toast.LENGTH_SHORT).show();
                    doCallOrMsg(MainActivity.this, MainActivity.this, contactBean);
                }
            });
        }
        btnAddUser = (Button) findViewById(R.id.btnAddUser);
        btnAddUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (GetPermission(MainActivity.this, MainActivity.this, Manifest.permission.WRITE_CONTACTS)) {
                    addUser();
                }
            }
        });


    }


    public boolean GetPermission(@NonNull Activity activity, @NonNull Context context, @NonNull String permission) {
        if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                TextView tvPermission=(TextView)findViewById(R.id.requirePermission);
                TextView tvSetting=(TextView)findViewById(R.id.tvSetting);
                TextView tvCancel=(TextView)findViewById(R.id.tvCancel);
                new AlertDialog.Builder(MainActivity.this)
                        .setMessage(tvPermission.getText().toString())
                        .setPositiveButton(tvSetting.getText().toString(), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                intent.setData(Uri.parse("package:" + getPackageName()));
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton(tvCancel.getText().toString(), null)
                        .create()
                        .show();
            } else {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.WRITE_CONTACTS},
                        REQUESTCODE);
            }
        } else {
            return true;
        }
        return false;
    }


    private void addUser() {
        View view = LayoutInflater.from(this).inflate(R.layout.adduser, null);
        final EditText et_name = (EditText) view.findViewById(R.id.etName);
        final EditText et_phone = (EditText) view.findViewById(R.id.etPhone);

        TextView tvCommit=(TextView)findViewById(R.id.tvCommit);
        TextView tvCancel=(TextView)findViewById(R.id.tvCancel);


        new AlertDialog.Builder(this)
                .setView(view)
                .setPositiveButton(tvCommit.getText().toString(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ContactBean contact = new ContactBean();
                        contact.setName(et_name.getText() + "");
                        contact.setPhone(et_phone.getText() + "");

                        ContactsOp.addContact(MainActivity.this, contact);
                        myAdapter = new ContactsAdapter(MainActivity.this, MainActivity.this, ContactsOp.getContacts(MainActivity.this));
                        lvContacts.setAdapter(myAdapter);
                    }
                })
                .setNegativeButton(tvCancel.getText().toString(), null)
                .show();
    }

    private void doCallOrMsg(@NonNull final Activity activity, @NonNull final Context context, @NonNull final ContactBean contactBean) {
        View view = View.inflate(this, R.layout.callormsg, null);
       /* final Button btnCall = (Button) view.findViewById(R.id.btnCall);
        final Button btnSendMsg = (Button) view.findViewById(R.id.btnSendMsg);*/
        TextView tvCall=(TextView)findViewById(R.id.tvCall);
        TextView tvMsg=(TextView)findViewById(R.id.tvMsg);
        new AlertDialog.Builder(this)
                .setTitle(R.string.tvCallorMsgTitle)
                .setItems(new String[]{tvCall.getText().toString(), tvMsg.getText().toString()}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + contactBean.getPhone()));
                            startActivity(intent);
                        } else if (which == 1) {
                            Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + contactBean.getPhone()));
                            intent.putExtra("compose_mode", true);
                            startActivity(intent);
                            finish();
                        }
                    }
                })
                .show();
      /*  new AlertDialog.Builder(this)
                .setView(view)
                .setItems(R.id.btnSendMsg, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(context,"发短息",Toast.LENGTH_SHORT).show();
                    }
                })
                .setItems(R.id.btnCall, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                         if(GetPermission(activity,context,Manifest.permission.CALL_PHONE)) {
                            Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + contactBean.getPhone()));
                            startActivity(intent);
                        }
                        Toast.makeText(context,"dadianhua",Toast.LENGTH_SHORT).show();


                    }
                })
                .setPositiveButton(R.string.btnCommit, null)
                .show();*/
    }

}
