package northern.captain.vendingman.reports;

import android.content.res.Resources;

import org.joda.time.*;
import org.joda.time.DateTime;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jxl.Workbook;
import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.format.UnderlineStyle;
import jxl.write.*;
import jxl.write.Number;
import jxl.write.biff.RowsExceededException;
import northern.captain.vendingman.AndroidContext;
import northern.captain.vendingman.R;
import northern.captain.vendingman.entities.Accounting;
import northern.captain.vendingman.entities.AccountingFactory;
import northern.captain.vendingman.entities.ReplGoodsView;
import northern.captain.vendingman.entities.ReplenishmentFactory;
import northern.captain.vendingman.entities.VendingMachine;

/**
 * Created by leo on 23.04.15.
 */
public class MaintenanceReport extends ReportBase
{
    VendingMachine machine;
    Date from;
    Date to;

    public MaintenanceReport(VendingMachine machine, Date from, Date to)
    {
        this.machine = machine;
        this.from = from;
        this.to = to;
    }

    @Override
    public ReportBase build()
    {
        try
        {
            Resources res = AndroidContext.mainActivity.getResources();

            File file = getFileToWrite("maintenance" + machine.id);
            WritableWorkbook workbook = Workbook.createWorkbook(file);

            WritableSheet sheet = workbook.createSheet(machine.getName(), 0);

            for (int i = 1; i < 18; i++)
            {
                setAutoSize(sheet, i);
            }

            String header = String.format(res.getString(R.string.rep_maintenance_head), machine.getName());
            Label label = new Label(0, 0, header, new WritableCellFormat(new WritableFont(WritableFont.ARIAL, 11, WritableFont.BOLD)));

            sheet.addCell(label);

            header = String.format(res.getString(R.string.rep_maintenance_period),
                    AndroidContext.repDateFormat.format(from), AndroidContext.repDateFormat.format(to));
            label = new Label(0, 1, header, new WritableCellFormat(new WritableFont(WritableFont.ARIAL, 10, WritableFont.BOLD)));
            sheet.addCell(label);
            sheet.mergeCells(0, 0, 8, 0);
            sheet.mergeCells(0, 1, 8, 1);

            label = new Label(0, 3, res.getString(R.string.rep_maintenance_repl),
                    new WritableCellFormat(new WritableFont(WritableFont.ARIAL, 10)));
            sheet.addCell(label);
            sheet.mergeCells(0, 3, 2, 3);

            WritableCellFormat capFormat = new WritableCellFormat(
                    new WritableFont(WritableFont.ARIAL, 10, WritableFont.BOLD, false, UnderlineStyle.NO_UNDERLINE, Colour.WHITE));
            capFormat.setAlignment(Alignment.CENTRE);
            capFormat.setBackground(Colour.TEAL);
            capFormat.setBorder(Border.ALL, BorderLineStyle.THIN);

            sheet.addCell(new Label(0, 4, "NN", capFormat));
            sheet.addCell(new Label(1, 4, res.getString(R.string.rep_goods_name), capFormat));

            WritableCellFormat tableFormat = new WritableCellFormat(
                    new WritableFont(WritableFont.ARIAL, 10));
            tableFormat.setBorder(Border.ALL, BorderLineStyle.THIN);

            WritableCellFormat tableFormatInt = new WritableCellFormat(
                    new WritableFont(WritableFont.ARIAL, 10), NumberFormats.INTEGER);
            tableFormatInt.setBorder(Border.ALL, BorderLineStyle.THIN);

            WritableCellFormat tableFormatIntB = new WritableCellFormat(
                    new WritableFont(WritableFont.ARIAL, 10, WritableFont.BOLD), NumberFormats.INTEGER);
            tableFormatIntB.setBorder(Border.ALL, BorderLineStyle.THIN);

            WritableCellFormat tableFormatIntBI = new WritableCellFormat(
                    new WritableFont(WritableFont.ARIAL, 11, WritableFont.BOLD), NumberFormats.INTEGER);
            tableFormatIntBI.setBorder(Border.ALL, BorderLineStyle.MEDIUM);

            List<ReplGoodsView> replRaw =
                    ReplenishmentFactory.instance.getReplenishmentsForMachine(machine.id,
                            from, to);

            groupRaw(replRaw);

            for(Map.Entry<String, Integer> entry : usedDatesSort.entrySet())
            {
                sheet.addCell(new Label(2 + entry.getValue(), 4, entry.getKey(), capFormat));
            }

            WritableCellFormat emptyCellFormat = new WritableCellFormat();
            emptyCellFormat.setBorder(Border.ALL, BorderLineStyle.THIN);

            //Clear all dynamic area, set border
            int dynRows = usedGoods.size();
            int dynCols = usedDatesSort.size();
            for(int y = 0; y<dynRows;y++)
            {
                for(int x = 0; x<dynCols;x++)
                {
                    sheet.addCell(new Label(x+2, y+5, "", emptyCellFormat));
                }
            }

            Map<Integer, Integer> sumQMap = new HashMap<Integer, Integer>();
            int maxCol = 0;
            int maxRow = 0;
            //Fill cells with values for those who have it
            for(ReplGoodsView view : qtyMap.values())
            {
                int col = usedDatesSort.get(view.startDateS) + 2;
                int row = usedGoods.get(view.goodsName) + 5;

                if(maxRow < row) maxRow = row;

                sheet.addCell(new Number(0, row, row - 4, tableFormatInt));
                sheet.addCell(new Label(1, row, view.goodsName, tableFormat));
                sheet.addCell(new Number(col, row, view.sumQty, tableFormatIntB));

                Integer qty = sumQMap.get(row);
                if(qty == null) qty = 0;
                qty += view.sumQty;
                sumQMap.put(row, qty);
                if(maxCol < col) maxCol = col;
            }

            maxCol++;
            //Last column - total sum for each row
            sheet.addCell(new Label(maxCol, 4, res.getString(R.string.rep_total), capFormat));
            for(Map.Entry<Integer, Integer> entry : sumQMap.entrySet())
            {
                sheet.addCell(new Number(maxCol, entry.getKey(), entry.getValue(), tableFormatIntB));
            }


            if(maxRow < 4) maxRow = 4;
            else
            {
                int sumTotal = 0;
                for(Integer value : sumQMap.values())
                {
                    sumTotal += value;
                }
                sheet.addCell(new Number(maxCol, ++maxRow, sumTotal, tableFormatIntBI));
                maxRow++;
            }

            //Accounting part of the report
            List<Accounting> accountings = getAccounting(from, to);

            groupRawAcc(accountings);

            maxRow++;
            maxRow++;
            label = new Label(0, maxRow, res.getString(R.string.rep_accounting_repl),
                    new WritableCellFormat(new WritableFont(WritableFont.ARIAL, 10)));
            sheet.addCell(label);
            sheet.mergeCells(0, maxRow, 2, maxRow);
            maxRow++;

            sheet.addCell(new Label(0, maxRow, "NN", capFormat));
            sheet.addCell(new Label(1, maxRow, res.getString(R.string.rep_accounting_what), capFormat));
            for(Map.Entry<String, Integer> entry : usedDatesSort.entrySet())
            {
                sheet.addCell(new Label(2 + entry.getValue(), maxRow, entry.getKey(), capFormat));
            }

            sheet.addCell(new Number(0, maxRow+1, 1, tableFormatInt));
            sheet.addCell(new Number(0, maxRow+2, 2, tableFormatInt));
            sheet.addCell(new Number(0, maxRow+3, 3, tableFormatInt));

            sheet.addCell(new Label(1, maxRow+1, res.getString(R.string.rep_accounting_count_total), tableFormat));
            sheet.addCell(new Label(1, maxRow+2, res.getString(R.string.rep_accounting_count_coins), tableFormat));
            sheet.addCell(new Label(1, maxRow+3, res.getString(R.string.rep_accounting_count_banknotes), tableFormat));

            for(Map.Entry<String, Accounting> entry : usedAccountings.entrySet())
            {
                int col = usedDatesSort.get(entry.getKey());
                sheet.addCell(new Number(col + 2, maxRow + 1, entry.getValue().getOtherQty(), tableFormatInt));
                sheet.addCell(new Number(col + 2, maxRow + 2, entry.getValue().getCoinsQty(), tableFormatInt));
                sheet.addCell(new Number(col + 2, maxRow + 3, entry.getValue().getMoneyQty(), tableFormatInt));
            }

            if(accountings.size()>1)
            {
                sheet.addCell(new Label(2 + usedDatesSort.size(), maxRow, res.getString(R.string.rep_accounting_inc), capFormat));

                Accounting one = accountings.get(0);
                Accounting two = accountings.get(1);

                int col = accountings.size();

                sheet.addCell(new Number(col + 2, maxRow + 1, Math.abs(one.getOtherQty() - two.getOtherQty()), tableFormatInt));
                sheet.addCell(new Number(col + 2, maxRow + 2, Math.abs(one.getCoinsQty() - two.getCoinsQty()), tableFormatInt));
                sheet.addCell(new Number(col + 2, maxRow + 3, Math.abs(one.getMoneyQty() - two.getMoneyQty()), tableFormatInt));
            }

            workbook.write();
            workbook.close();

            openFile(file);
        } catch (IOException e)
        {
            e.printStackTrace();
        } catch (RowsExceededException e)
        {
            e.printStackTrace();
        } catch (WriteException e)
        {
            e.printStackTrace();
        }

        return this;
    }

    List<Accounting> getAccounting(Date from, Date to)
    {
        org.joda.time.DateTime dateFrom = new DateTime(from).withTime(0, 0, 0, 0);
        org.joda.time.DateTime dateTo = new DateTime(to).withTime(0, 0, 0, 0);

        List<Accounting> ret = new ArrayList<Accounting>();

        List<Accounting> list = AccountingFactory.instance.getAccountingListByDates(
                machine.id, dateFrom.minusMonths(2).toDate(), dateFrom.plusDays(2).toDate());

        if(!list.isEmpty())
        {
            ret.add(list.get(list.size()-1));
        }

        list = AccountingFactory.instance.getAccountingListByDates(
                machine.id, dateFrom.plusDays(2).toDate(), dateTo.plusDays(15).toDate());

        if(!list.isEmpty())
        {
            if(ret.isEmpty() && list.size()>1)
            {
                ret.add(list.get(0));
            }
            ret.add(list.get(list.size()-1));
        }

        return ret;
    }

    Map<String, Accounting> usedAccountings = new HashMap<String, Accounting>();

    private void groupRawAcc(List<Accounting> rawList)
    {
        for(Accounting accounting : rawList)
        {
            String dateS = AndroidContext.repDateFormat.format(accounting.createdDate);
            if(usedAccountings.containsKey(dateS)) continue;

            usedAccountings.put(dateS, accounting);
        }

        List<Accounting> dates = new ArrayList<Accounting>();
        dates.addAll(usedAccountings.values());
        Collections.sort(dates, new Comparator<Accounting>()
        {
            @Override
            public int compare(Accounting accounting, Accounting accounting2)
            {
                return accounting.createdDate.compareTo(accounting2.createdDate);
            }
        });

        usedDatesSort.clear();
        for(int i=0;i<dates.size();i++)
        {
            usedDatesSort.put(AndroidContext.repDateFormat.format(dates.get(i).createdDate), i);
        }

    }

    Map<String, Integer> usedGoods = new HashMap<String, Integer>();
    Map<String, Integer> usedDatesSort = new HashMap<String, Integer>();
    Map<String, Date> usedDates = new HashMap<String, Date>();
    Map<String, ReplGoodsView> qtyMap = new HashMap<String, ReplGoodsView>();

    private void groupRaw(List<ReplGoodsView> rawList)
    {
        for(ReplGoodsView view : rawList)
        {
            usedGoods.put(view.goodsName, 0);
            usedDates.put(view.startDateS, view.startDate);

            String key = view.getKey();

            ReplGoodsView sumView = qtyMap.get(key);
            if(sumView == null)
            {
                sumView = view;
            }
            sumView.sumQty += view.qty;
            qtyMap.put(key, sumView);
        }

        List<String> goods = new ArrayList<String>();
        goods.addAll(usedGoods.keySet());
        Collections.sort(goods);

        for(int i=0;i<goods.size();i++)
        {
            usedGoods.put(goods.get(i), i);
        }

        List<Date> dates = new ArrayList<Date>();
        dates.addAll(usedDates.values());
        Collections.sort(dates);

        for(int i=0;i<dates.size();i++)
        {
            usedDatesSort.put(AndroidContext.repDateFormat.format(dates.get(i)), i);
        }
    }
}
