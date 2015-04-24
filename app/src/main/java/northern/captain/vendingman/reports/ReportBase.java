package northern.captain.vendingman.reports;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import jxl.CellView;
import jxl.write.WritableSheet;
import northern.captain.vendingman.AndroidContext;
import northern.captain.vendingman.R;

/**
 * Created by leo on 23.04.15.
 */
public abstract class ReportBase
{
    protected static final SimpleDateFormat FILE_DATE_F = new SimpleDateFormat("yyyy-MM-dd_HH_mm");

    protected void openFile(File file)
    {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file),"application/vnd.ms-excel");
        Intent chooser = Intent.createChooser(intent, AndroidContext.mainActivity.getResources().getString(R.string.open_xls));
        AndroidContext.mainActivity.startActivity(chooser);
    }

    protected File getFileToWrite(String baseName)
    {
        File path = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS);
        path = new File(path, "VendingReports");
        File file = new File(path, baseName + "-" + FILE_DATE_F.format(new Date()) + ".xls");

        try
        {
            // Make sure the Pictures directory exists.
            path.mkdirs();
        }
        catch (Exception ex)
        {}

        return file;
    }

    protected CellView setAutoSize(WritableSheet sheet, int col)
    {
        CellView view = sheet.getColumnView(col);
        view.setAutosize(true);
        sheet.setColumnView(col, view);
        return view;
    }

    public abstract ReportBase build();
}
