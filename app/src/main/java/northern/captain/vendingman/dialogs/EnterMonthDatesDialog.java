package northern.captain.vendingman.dialogs;

import android.content.DialogInterface;
import android.content.res.Resources;
import android.support.v4.app.DialogFragment;
import android.widget.Button;

import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.sleepbot.datetimepicker.time.RadialPickerLayout;
import com.sleepbot.datetimepicker.time.TimePickerDialog;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.joda.time.DateTime;
import org.joda.time.MutableDateTime;

import java.text.SimpleDateFormat;
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
@EFragment(R.layout.edit_report_dates_dialog)
public class EnterMonthDatesDialog extends DialogFragment
{
    private static final SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM");

    @ViewById(R.id.dates_rep_fromdate_but)
    Button fromDateBut;

    @ViewById(R.id.dates_rep_todate_but)
    Button toDateBut;

    @ViewById(R.id.dates_rep_month0_but)
    Button month0But;

    @ViewById(R.id.dates_rep_month1_but)
    Button month1But;

    @ViewById(R.id.dates_rep_month2_but)
    Button month2But;

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

    @AfterViews
    void initViews()
    {
        if(titleResId != 0)
        {
            getDialog().setTitle(titleResId);
        }

        transactionFromDate.setDate(getStartMonth(DateTime.now(), 0));
        transactionFromDate.setTime(0, 0, 0, 0);
        transactionToDate.setDate(getStartMonth(DateTime.now(), 1));
        transactionToDate.setTime(0, 0, 0, 0);

        Resources res = getResources();

        month0But.setText(String.format(res.getString(R.string.choose_cur_month),
                monthFormat.format(getStartMonth(DateTime.now(), 0).toDate())));
        month1But.setText(String.format(res.getString(R.string.choose_prev_month),
                monthFormat.format(getStartMonth(DateTime.now(), -1).toDate())));
        month2But.setText(String.format(res.getString(R.string.choose_preprev_month),
                monthFormat.format(getStartMonth(DateTime.now(), -2).toDate())));

        initDates();

//        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    public Date getFromDate()
    {
        return transactionFromDate.toDate();
    }

    public Date getToDate()
    {
        return transactionToDate.toDate();
    }

    private DateTime getStartMonth(DateTime fromDate, int addMonths)
    {
        MutableDateTime date1 = new MutableDateTime(fromDate);
        date1.setDayOfMonth(1);
        date1.setTime(0, 0, 0, 0);
        date1.addMonths(addMonths);
        return date1.toDateTime();
    }

    private void initDates()
    {
        fromDateBut.setText(Helpers.smartDateString(transactionFromDate.toDate()));
        toDateBut.setText(Helpers.smartDateString(transactionToDate.toDate()));
    }

    private int titleResId = 0;
    private String text;

    public void setTitle(int resId)
    {
        titleResId = resId;
    }

    @Click(R.id.dates_rep_ok_but)
    void onOkClick()
    {
        if(callback != null)
        {
            callback.run();
        }
        dismiss();
    }

    @Click(R.id.dates_rep_cancel_but)
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

    MutableDateTime transactionFromDate = new MutableDateTime();
    MutableDateTime transactionToDate = new MutableDateTime();

    @Click(R.id.dates_rep_fromdate_but)
    void onFromDateClicked()
    {
        final DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(
                new DatePickerDialog.OnDateSetListener()
                {
                    @Override
                    public void onDateSet(DatePickerDialog datePickerDialog, int year, int month, int day)
                    {
                        transactionFromDate.setYear(year);
                        transactionFromDate.setMonthOfYear(month + 1);
                        transactionFromDate.setDayOfMonth(day);
                        initDates();
                    }
                },
                transactionFromDate.getYear(),
                transactionFromDate.getMonthOfYear()-1,
                transactionFromDate.getDayOfMonth(), true);
        datePickerDialog.show(getFragmentManager(), "date");
    }

    @Click(R.id.dates_rep_todate_but)
    void onToDateClicked()
    {
        final DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(
                new DatePickerDialog.OnDateSetListener()
                {
                    @Override
                    public void onDateSet(DatePickerDialog datePickerDialog, int year, int month, int day)
                    {
                        transactionToDate.setYear(year);
                        transactionToDate.setMonthOfYear(month+1);
                        transactionToDate.setDayOfMonth(day);
                        initDates();
                    }
                },
                transactionToDate.getYear(),
                transactionToDate.getMonthOfYear()-1,
                transactionToDate.getDayOfMonth(), true);
        datePickerDialog.show(getFragmentManager(), "date");
    }

    @Click(R.id.dates_rep_month0_but)
    public void onMonth0Clicked()
    {
        transactionFromDate.setDate(getStartMonth(DateTime.now(), 0));
        transactionToDate.setDate(getStartMonth(DateTime.now(), 1));

        initDates();
        onOkClick();
    }

    @Click(R.id.dates_rep_month1_but)
    public void onMonth1Clicked()
    {
        transactionFromDate.setDate(getStartMonth(DateTime.now(), -1));
        transactionToDate.setDate(getStartMonth(DateTime.now(), 0));

        initDates();
        onOkClick();
    }

    @Click(R.id.dates_rep_month2_but)
    public void onMonth2Clicked()
    {
        transactionFromDate.setDate(getStartMonth(DateTime.now(), -2));
        transactionToDate.setDate(getStartMonth(DateTime.now(), -1));

        initDates();
        onOkClick();
    }
}
