package com.groceryExpiry;

import java.util.Date;
import java.util.List;

import com.dm.zbar.android.scanner.ZBarConstants;
import com.dm.zbar.android.scanner.ZBarScannerActivity;
import com.groceryExpiry.models.*;
import com.groceryExpiry.sql.FoodItemDataSource;

import android.os.Bundle;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;


public class FoodItemListActivity extends Activity {

	private ArrayAdapter<FoodItem> adapter;
	private FoodItemDataSource datasource;
	private boolean useAlphaSort;
	private Button sortButton;
    private static final int ZBAR_SCANNER_REQUEST = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_item_list);
        
        Button scanButton = (Button) findViewById(R.id.scan_btn);
        Log.v(" Camera is available",scanButton.toString());
        scanButton.setOnClickListener(new View.OnClickListener(){
        	
        	public void onClick(View v) {
    			//Intent intent = new Intent(FoodItemListActivity.this, ScannerActivity.class);
                    if (isCameraAvailable()) {
                    	Log.v(" Camera is available","in here");
                        Intent intent = new Intent(FoodItemListActivity.this, ZBarScannerActivity.class);
                        
                        
                        startActivityForResult(intent, ZBAR_SCANNER_REQUEST);
                    } else {
                        Toast.makeText(FoodItemListActivity.this, "Rear Facing Camera Unavailable", Toast.LENGTH_SHORT).show();
                    }
                }
                
                public boolean isCameraAvailable() {
                	Log.v("in Camera available","in here");
                    PackageManager pm = getPackageManager();
                    return pm.hasSystemFeature(PackageManager.FEATURE_CAMERA);
                }
                
                /*	public void onClick(View v) {
            			Intent intent = new Intent(FoodItemListActivity.this, ScannerActivity.class);

        				startActivityForResult(intent, 0);
                    }*/
                
				//startActivityForResult(intent, 0);
            }
        	
        );
        
        Button addItemButton = (Button) findViewById(R.id.button_add_item);
        
        addItemButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Log.v(" Camera is available","in here");
				Intent intent = new Intent(FoodItemListActivity.this, AddItemActivity.class);
				startActivityForResult(intent, 0);
				
			}
		});
        

        
        ListView lv = (ListView) findViewById(R.id.foodlist);
        
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        	
        	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        		
        		FoodItemAdapter.FoodItemHolder h = (FoodItemAdapter.FoodItemHolder) view.getTag();
        		long foodItemID = h.foodItem.getId();
        		
        		Intent intent = new Intent(FoodItemListActivity.this, EditItemActivity.class);
        		intent.putExtra("ID", foodItemID);
        		
        		startActivityForResult(intent, 1);
        		
        	}
        	
		});
        
        sortButton = (Button) findViewById(R.id.button_sortalpha);
        
        sortButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				FoodItemListActivity.this.toogleSort();
			}
		});
        
        dBUpdateArrayAdapter();
        
    }
    
    protected void toogleSort() {
		useAlphaSort = !useAlphaSort;
		if (useAlphaSort) {
			sortButton.setText(R.string.button_sortexpiry);
		} else {
			sortButton.setText(R.string.button_sortalpha);
		}
		
		dBUpdateArrayAdapter();
	}

	@Override
    public void onResume() {
    	
    	//dBUpdateArrayAdapter();
    	
    	super.onResume();
    	
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	Log.v(" Camera is available","on activity result");
    	//Log.v(String.valueOf(requestCode), "hi");
    	//Log.v(String.valueOf(RESULT_OK), "hi here");
	    if (resultCode == RESULT_OK) 
	    {
	    	
	    	Log.v(" Camera is available","result ok");
	        // Scan result is available by making a call to data.getStringExtra(ZBarConstants.SCAN_RESULT)
	        // Type of the scan result is available by making a call to data.getStringExtra(ZBarConstants.SCAN_RESULT_TYPE)
	        Toast.makeText(FoodItemListActivity.this, "Scan Result = " + data.getStringExtra(ZBarConstants.SCAN_RESULT), Toast.LENGTH_SHORT).show();
	        Toast.makeText(FoodItemListActivity.this, "Scan Result Type = " + data.getStringExtra(ZBarConstants.SCAN_RESULT_TYPE), Toast.LENGTH_SHORT).show();
	        // The value of type indicates one of the symbols listed in Advanced Options below.
	    } else if(resultCode == RESULT_CANCELED) {
	    	Log.v(" Camera is available","on activity cancelled");
	        Toast.makeText(FoodItemListActivity.this, "Camera unavailable", Toast.LENGTH_SHORT).show();
	    }
    	
    	dBUpdateArrayAdapter();
    	
    	super.onActivityResult(requestCode, resultCode, data);
    	
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.food_item_list, menu);
        
        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
			
			@Override
			public boolean onQueryTextSubmit(String query) {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public boolean onQueryTextChange(String newText) {
				
				FoodItemDataSource datasource = new FoodItemDataSource(FoodItemListActivity.this);
		  		datasource.open();
		  		
		  		List<FoodItem> items = datasource.searchFoodItems(newText);
		  		
		  		datasource.close();
		          
		        searchUpdateArrayAdapter(items);
				
				return false;
			}
		});
        
        return true;
        
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	
    	//Attach Add Item Button Listener...
    	switch(item.getItemId()) {
    	
	    	default:
	    		return super.onOptionsItemSelected(item);
    	
    	}
    	
    }
    
    public ArrayAdapter<FoodItem> getFoodItemAdapter() {
    	
    	return adapter;
    	
    }
    
    public void searchUpdateArrayAdapter(List<FoodItem> items) {
    	
    	ListView listview = (ListView) findViewById(R.id.foodlist);
    	
    	adapter = new FoodItemAdapter(this,
        		R.layout.food_item_cell, items);
    	
        listview.setAdapter(adapter);
    	
    }
    
    private void dBUpdateArrayAdapter() {
    	
    	datasource = new FoodItemDataSource(this);
        datasource.open();
    	
    	List<FoodItem> items = datasource.getAllFoodItems(useAlphaSort);
        
        //Populate ListView...
        ListView listview = (ListView) findViewById(R.id.foodlist);
        
        adapter = new FoodItemAdapter(this,
        		R.layout.food_item_cell, items);
    	
        listview.setAdapter(adapter);
    	
    	datasource.close();
    	
    }
    
    @Override
    public void onNewIntent(Intent intent) {
    	
    	//setIntent(intent);
    	//searchFoodItems(intent);
    	
  	}

  	private void searchFoodItems(Intent intent) {
  		
  		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
  			
    	    String query = intent.getStringExtra(SearchManager.QUERY);
  		
	  		FoodItemDataSource datasource = new FoodItemDataSource(this);
	  		datasource.open();
	  		
	  		List<FoodItem> items = datasource.searchFoodItems(query);
	  		
	  		datasource.close();
	          
	        searchUpdateArrayAdapter(items);
        
  		}
  		
  	}
    
}
