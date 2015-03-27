package northern.captain.vendingman.dialogs;

import android.content.DialogInterface;
import android.support.v4.app.DialogFragment;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.sleepbot.datetimepicker.time.RadialPickerLayout;
import com.sleepbot.datetimepicker.time.TimePickerDialog;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.util.Calendar;
import java.util.Date;

import northern.captain.vendingman.AndroidContext;
import northern.captain.vendingman.R;
import northern.captain.vendingman.entities.Maintenance;
import northern.captain.vendingman.entities.MaintenanceFactory;
import northern.captain.vendingman.tools.Helpers;

/**
 * Created by leo on 20.11.14.
 */
@EFragment(R.layout.edit_maint_dates_dialog)
public class EnterMaintenanceDatesDialog extends DialogFragment
{
    @ViewById(R.id.dates_maint_fromdate_but)
    Button fromDateBut;

    @ViewById(R.id.dates_maint_fromtime_but)
    Button fromTimeBut;

    @ViewById(R.id.dates_maint_todate_but)
    Button toDateBut;

    @ViewById(R.id.dates_maint_totime_but)
    Button toTimeBut;

    Runnable callback;
    ICancelCallback cancelCallback;

    public void setCancelCallback(ICancelCallback cancelCallback)
    {
        this.cancelCallback = cancelCallback;
    }

    public void setCallback(Runnable callback)
    {
        this.callback = callback;
    }

    Maintenance maintenance;

    public void setMaintenance(Maintenance maintenance)
    {
        this.maintenance = maintenance;
    }

    @AfterViews
    void initViews()
    {
        if(titleResId != 0)
        {
            getDialog().setTitle(titleResId);
        }

        transactionFromDate.setTime(maintenance.startDate);
        transactionToDate.setTime(maintenance.finishDate);
        initDates();


//        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    private void initDates()
    {
        fromDateBut.setText(Helpers.smartDateString(transactionFromDate.getTime()));
        fromTimeBut.setText(AndroidContext.timeFormat.format(transactionFromDate.getTime()));

        toDateBut.setText(Helpers.smartDateString(transactionToDate.getTime()));
        toTimeBut.setText(AndroidContext.timeFormat.format(transactionToDate.getTime()));
    }

    private int titleResId = 0;
    private String text;

    public void setTitle(int resId)
    {
        titleResId = resId;
    }

    @Click(R.id.dates_maint_ok_but)
    void onOkClick()
    {
        maintenance.startDate = transactionFromDate.getTime();
        maintenance.finishDate = transactionToDate.getTime();
        MaintenanceFactory.instance.update(maintenance);
        if(callback != null)
        {
            callback.run();
        }
        dismiss();
    }

    @Click(R.id.dates_maint_cancel_but)
    void onCancelClick()
    {
        dismiss();
        if(cancelCallback != null)
        {
            cancelCallback.cancel();
        }
    }

    @Override
    public void onCancel(DialogInterface dialog)
    {
        super.onCancel(dialog);
        if(cancelCallback != null)
        {
            cancelCallback.cancel();
        }
    }

    final Calendar transactionFromDate = Calendar.getInstance();
    final Calendar transactionToDate = Calendar.getInstance();

    @Click(R.id.dates_maint_fromdate_but)
    void onFromDateClicked()
    {
        final DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(
                new DatePickerDialog.OnDateSetListener()
                {
                    @Override
                    public void onDateSet(DatePickerDialog datePickerDialog, int year, int month, int day)
                    {
                        transactionFromDate.set(year, month, day);
                        initDates();
                    }
                },
                transactionFromDate.get(Calendar.YEAR),
                transactionFromDate.get(Calendar.MONTH),
                transactionFromDate.get(Calendar.DAY_OF_MONTH), true);
        datePickerDialog.show(getFragmentManager(), "date");
    }

    @Click(R.id.dates_maint_todate_but)
    void onToDateClicked()
    {
        final DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(
                new DatePickerDialog.OnDateSetListener()
                {
                    @Override
                    public void onDateSet(DatePickerDialog datePickerDialog, int year, int month, int day)
                    {
                        transactionToDate.set(year, month, day);
                        initDates();
                    }
                },
                transactionToDate.get(Calendar.YEAR),
                transactionToDate.get(Calendar.MONTH),
                transactionToDate.get(Calendar.DAY_OF_MONTH), true);
        datePickerDialog.show(getFragmentManager(), "date");
    }

    @Click(R.id.dates_maint_totime_but)
    void onToTimeClicked()
    {
        final TimePickerDialog timePickerDialog = TimePickerDialog.newInstance(
                new TimePickerDialog.OnTimeSetListener()
                {
                    @Override
                    public void onTimeSet(RadialPickerLayout radialPickerLayout, int hours, int mins)
                    {
                        transactionToDate.set(Calendar.HOUR_OF_DAY, hours);
                        transactionToDate.set(Calendar.MINUTE, mins);
                        initDates();
                    }
                },
                transactionToDate.get(Calendar.HOUR_OF_DAY),
                transactionToDate.get(Calendar.MINUTE), true, false);

        timePickerDialog.show(getFragmentManager(), "time");
    }

    @Click(R.id.dates_maint_fromtime_but)
    void onFromTimeClicked()
    {
        final TimePickerDialog timePickerDialog = TimePickerDialog.newInstance(
                new TimePickerDialog.OnTimeSetListener()
                {
                    @Override
                    public void onTimeSet(RadialPickerLayout radialPickerLayout, int hours, int mins)
                    {
                        transactionFromDate.set(Calendar.HOUR_OF_DAY, hours);
                        transactionFromDate.set(Calendar.MINUTE, mins);
                        initDates();
                    }
                },
                transactionFromDate.get(Calendar.HOUR_OF_DAY),
                transactionFromDate.get(Calendar.MINUTE), true, false);
        timePickerDialog.show(getFragmentManager(), "time");
    }
}
