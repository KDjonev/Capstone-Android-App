// Some reference code for using custom pins for the pin drop.
// The icons are png pictures in the res/drawable/ folder.

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
/*
public void placeMarker(LatLng latLng, Icon i)
{
    int deviceW = getResources().getDisplayMetrics().widthPixels;
    int deviceH = getResources().getDisplayMetrics().heightPixels;
    int scale = Math.min(deviceH,deviceW);
    Log.i("size", "width = " + deviceW + " and height = " + deviceH);
    int newWidth = scale/10;
    int newHeight = scale/10;
    Log.i("size", "width = " + newWidth + " and height = " + newHeight);
    MarkerOptions markerOptions = new MarkerOptions();
    markerOptions.position(latLng);
    markerOptions.title("Position");
    Bitmap b;

    switch(i)
    {
        case START_FLAG:
            b = resizeBitmap(R.drawable.start_flag, newWidth, newHeight);
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(b));
            markerOptions.title("Start Position");
            startLocation = mMap.addMarker(markerOptions);
            return;
        case BIKER:
            //markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.biker));
            b = resizeBitmap(R.drawable.biker, newWidth, newHeight);
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(b));
            break;
        case HIKER:
            b = resizeBitmap(R.drawable.hiker, newWidth, newHeight);
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(b));
            break;
        case RUN_MAN:
            b = resizeBitmap(R.drawable.run_man, newWidth, newHeight);
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(b));
            break;
        case WALK_MAN:
            b = resizeBitmap(R.drawable.walk_man, newWidth, newHeight);
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(b));
            break;
        case FINISH_FLAG:
            if (finishLocation != null) {
                finishLocation.remove();
            }
            b = resizeBitmap(R.drawable.finish_flag, newWidth, newHeight);
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(b));
            markerOptions.title("End Position");
            finishLocation = mMap.addMarker(markerOptions);
            return;
    }
    mCurrLocation = mMap.addMarker(markerOptions);
}

private Bitmap resizeBitmap(int id, int w, int h)
{
    Bitmap bitMap = BitmapFactory.decodeResource(getResources(),id,null);
    Bitmap resized = Bitmap.createScaledBitmap(bitMap, w, h, true);
    return resized;
}
*/