package cn.mucang.genrationcompareheader;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private BjHeaderMenuView mListView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        final ArrayList<Contact> contacts = createContactList(20);
        final MyAdapter adapter = new MyAdapter(this, contacts);

        mListView = (BjHeaderMenuView)findViewById(R.id.my_list);
        mListView.setAdapter(adapter);
        mListView.setDynamics(new SimpleDynamics(0.8f, 0.8f));

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(final AdapterView<?> parent, final View view,
                                    final int position, final long id) {
                final String message = "OnClick: " + contacts.get(position).mName;
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                startActivity(new Intent(MainActivity.this, TestActivity.class));
                finish();
            }
        });

        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(final AdapterView<?> parent, final View view,
                                           final int position, final long id) {
                final String message = "OnLongClick: " + contacts.get(position).mName;
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Creates a list of fake contacts
     *
     * @param size How many contacts to create
     * @return A list of fake contacts
     */
    private ArrayList<Contact> createContactList(final int size) {
        final ArrayList<Contact> contacts = new ArrayList<Contact>();
        for (int i = 0; i < size; i++) {
            contacts.add(new Contact("Contact Number " + i, "+46(0)"
                    + (int)(1000000 + 9000000 * Math.random())));
        }
        return contacts;
    }

    /**
     * Adapter class to use for the list
     */
    private static class MyAdapter extends ArrayAdapter<Contact> {

        /** Re-usable contact image drawable */
        private final Drawable contactImage;

        /**
         * Constructor
         *
         * @param context The context
         * @param contacts The list of contacts
         */
        public MyAdapter(final Context context, final ArrayList<Contact> contacts) {
            super(context, 0, contacts);
            contactImage = context.getResources().getDrawable(R.drawable.contact_image);
        }

        @Override
        public View getView(final int position, final View convertView, final ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = LayoutInflater.from(getContext()).inflate(R.layout.list_item, null);
            }

            final TextView name = (TextView)view.findViewById(R.id.contact_name);
            if (position == 14) {
                name.setText("This is a long text that will make this box big. "
                        + "Really big. Bigger than all the other boxes. Biggest of them all.");
            } else {
                name.setText(getItem(position).mName);
            }

            final ImageView photo = (ImageView)view.findViewById(R.id.contact_photo);
            photo.setImageDrawable(contactImage);

            photo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getContext(),"点击position=》"+position,Toast.LENGTH_SHORT).show();
                }
            });

            return view;
        }
    }


    private static class Contact {

        /** Name of the contact */
        String mName;

        /** Phone number of the contact */
        String mNumber;

        /**
         * Constructor
         *
         * @param name The name
         * @param number The number
         */
        public Contact(final String name, final String number) {
            mName = name;
            mNumber = number;
        }

    }


    /**
     * A very simple dynamics implementation with spring-like behavior
     */
    class SimpleDynamics extends Dynamics {

        /** The friction factor */
        private float mFrictionFactor;

        /** The snap to factor */
        private float mSnapToFactor;

        /**
         * Creates a SimpleDynamics object
         *
         * @param frictionFactor The friction factor. Should be between 0 and 1.
         *            A higher number means a slower dissipating speed.
         * @param snapToFactor The snap to factor. Should be between 0 and 1. A
         *            higher number means a stronger snap.
         */
        public SimpleDynamics(final float frictionFactor, final float snapToFactor) {
            mFrictionFactor = frictionFactor;
            mSnapToFactor = snapToFactor;
        }

        @Override
        protected void onUpdate(final int dt) {
            // update the velocity based on how far we are from the snap point
            mVelocity += getDistanceToLimit() * mSnapToFactor;

            // then update the position based on the current velocity
            mPosition += mVelocity * dt / 1000;

            // and finally, apply some friction to slow it down
            mVelocity *= mFrictionFactor;
        }
    }
}
