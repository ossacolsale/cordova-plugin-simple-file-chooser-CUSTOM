package in.foobars.cordova;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.DocumentsContract;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.BufferedReader;
import java.lang.Exception;
import java.nio.charset.CoderResult;
import java.util.ArrayList;
import java.util.List;
import java.io.File;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Chooser extends CordovaPlugin {
    private static final String ACTION_OPEN = "getFiles";
    private static final String ACTION_GRANT_DIR = "grantDir";
    private static final int PICK_FILE_REQUEST = 1;
    private static final int GRANT_DIR_REQUEST = 2;
    private static final String TAG = "Chooser";

    public static String getDisplayName(ContentResolver contentResolver, Uri uri) {
        String[] projection = {MediaStore.MediaColumns.DISPLAY_NAME};
        Cursor metaCursor = contentResolver.query(uri, projection, null, null, null);

        if (metaCursor != null) {
            try {
                if (metaCursor.moveToFirst()) {
                    return metaCursor.getString(0);
                }
            } finally {
                metaCursor.close();
            }
        }

        return null;
    }

    private CallbackContext callback;

    public void grantDirectory(CallbackContext callbackContext, String startFileUri) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);

        Uri fileUri = Uri.parse(startFileUri);
        File file= new File(fileUri.getPath());
        String startUri = startFileUri.split(file.getName())[0];

        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, Uri.parse(startUri));
        
        /*intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);*/

        Intent chooser = Intent.createChooser(intent, "Select File");
        cordova.startActivityForResult(this, chooser, Chooser.GRANT_DIR_REQUEST);

        //cordova.startActivityForResult(this, intent, Chooser.GRANT_DIR_REQUEST);

        PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
        pluginResult.setKeepCallback(true);
        this.callback = callbackContext;
        callbackContext.sendPluginResult(pluginResult);
    }

    public void chooseFile(CallbackContext callbackContext, String accept) {
        //Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);

        intent.putExtra(Intent.EXTRA_MIME_TYPES, accept.split(","));
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        //intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        

        Intent chooser = Intent.createChooser(intent, "Select File");
        cordova.startActivityForResult(this, chooser, Chooser.PICK_FILE_REQUEST);

        PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
        pluginResult.setKeepCallback(true);
        this.callback = callbackContext;
        callbackContext.sendPluginResult(pluginResult);
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) {
        try {
            if (action.equals(Chooser.ACTION_OPEN)) {
                this.chooseFile(callbackContext, args.getString(0));
                return true;
            } else if (action.equals(Chooser.ACTION_GRANT_DIR)) {
                this.grantDirectory(callbackContext, args.getString(0));
                return true;
            } else if (action.equals("readFile")) {
                this.readFileAction(callbackContext, Uri.parse(args.getString(0)));
                return true;
            }
        } catch (JSONException err) {
            this.callback.error("Execute failed: " + err.toString());
        }

        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            if (requestCode == Chooser.PICK_FILE_REQUEST && this.callback != null) {
                if (resultCode == Activity.RESULT_OK) {
                    final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
                    JSONArray files = new JSONArray();
                    if (data.getClipData() != null) {
                        for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                            Uri uri = data.getClipData().getItemAt(i).getUri();
                            this.cordova.getActivity().getContentResolver().takePersistableUriPermission(uri, takeFlags);
                            files.put(processFileUri(uri));
                            
                        }
                        this.callback.success(files.toString());
                    } else if (data.getData() != null) {
                        Uri uri = data.getData();
                        this.cordova.getActivity().getContentResolver().takePersistableUriPermission(uri, takeFlags);
                        files.put(processFileUri(uri));
                        this.callback.success(files.toString());
                    } else {
                        this.callback.error("File URI was null.");
                    }
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    this.callback.error("RESULT_CANCELED");
                } else {
                    this.callback.error(resultCode);
                }
            } else
            if (requestCode == Chooser.GRANT_DIR_REQUEST && this.callback != null) {
                if (resultCode == Activity.RESULT_OK) {
                    final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
                    if (data.getClipData() != null) {
                        for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                            Uri uri = data.getClipData().getItemAt(i).getUri();
                            this.cordova.getActivity().getContentResolver().takePersistableUriPermission(uri, takeFlags);
                        }
                        this.callback.success("ok");
                    } else if (data.getData() != null) {
                        Uri uri = data.getData();
                        this.cordova.getActivity().getContentResolver().takePersistableUriPermission(uri, takeFlags);
                        this.callback.success("ok");
                    } else {
                        this.callback.error("Directory URI was null.");
                    }
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    this.callback.error("RESULT_CANCELED");
                } else {
                    this.callback.error(resultCode);
                }
            }
        } catch (Exception err) {
            this.callback.error("Failed to read file: " + err.toString());
        }
    }

    public JSONObject processFileUri(Uri uri) {
        ContentResolver contentResolver = this.cordova.getActivity().getContentResolver();

        String name = Chooser.getDisplayName(contentResolver, uri);
        String mediaType = contentResolver.getType(uri);
        if (mediaType == null || mediaType.isEmpty()) {
            mediaType = "application/octet-stream";
        }
        JSONObject file = new JSONObject();
        try {
            file.put("mediaType", mediaType);
            file.put("name", name);
            file.put("uri", uri.toString());
            file.put("content", readFile(uri));
        } catch (JSONException err) {
            this.callback.error("Processing failed: " + err.toString());
        }
        return file;
    }

    private String readFile (Uri uri) {
        try {
            ContentResolver contentResolver = this.cordova.getActivity().getContentResolver();
            InputStream in = contentResolver.openInputStream(uri);
            BufferedReader r = new BufferedReader(new InputStreamReader(in));
            StringBuilder total = new StringBuilder();
            for (String line; (line = r.readLine()) != null; ) {
                total.append(line).append('\n');
            }
    
            return total.toString();
    
    
    
        }catch (Exception e) {
            return null;
        }
    }

    private void readFileAction (CallbackContext callbackContext, Uri uri) {
        try {
            String content = this.readFile(uri);
            callbackContext.success(content);
        } catch (Exception e) {
            callbackContext.error(e.getMessage());
            e.printStackTrace();
        }
    }

}
