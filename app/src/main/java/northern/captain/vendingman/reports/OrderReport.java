package northern.captain.vendingman.reports;

import android.content.res.Resources;

import java.io.File;
import java.io.IOException;
import java.util.List;

import jxl.Workbook;
import jxl.format.*;
import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.write.*;
import jxl.write.Number;
import jxl.write.biff.RowsExceededException;
import northern.captain.vendingman.AndroidContext;
import northern.captain.vendingman.R;
import northern.captain.vendingman.entities.Order;
import northern.captain.vendingman.entities.OrderDetItem;

/**
 * Created by leo on 23.04.15.
 */
public class OrderReport extends ReportBase
{
    Order order;
    List<? extends OrderDetItem> items;

    public OrderReport(Order order, List<? extends OrderDetItem> items)
    {
        this.order = order;
        this.items = items;
    }

    private File getFileToWrite()
    {
        return getFileToWrite("Order" + order.id);
    }

    @Override
    public OrderReport build()
    {
        try
        {
            Resources res = AndroidContext.mainActivity.getResources();

            File file = getFileToWrite();
            WritableWorkbook workbook = Workbook.createWorkbook(file);

            WritableSheet sheet = workbook.createSheet(res.getString(R.string.order_num) + order.id, 0);

            for(int i=1;i<3;i++)
            {
                setAutoSize(sheet, i);
            }

            String header = String.format(res.getString(R.string.rep_order_header), order.id, AndroidContext.dateFormat.format(order.createdDate));
            Label label = new Label(0, 0, header, new WritableCellFormat(new WritableFont(WritableFont.ARIAL, 11, WritableFont.BOLD)));

            sheet.addCell(label);
            sheet.mergeCells(0, 0, 8, 0);

            WritableCellFormat capFormat = new WritableCellFormat(
                    new WritableFont(WritableFont.ARIAL, 10, WritableFont.BOLD, false, UnderlineStyle.NO_UNDERLINE, Colour.WHITE));
            capFormat.setAlignment(Alignment.CENTRE);
            capFormat.setBackground(Colour.VIOLET);
            capFormat.setBorder(Border.ALL, BorderLineStyle.THIN);

            sheet.addCell(new Label(0, 2, "NN", capFormat));
            sheet.addCell(new Label(1, 2, res.getString(R.string.rep_goods_name), capFormat));
            sheet.addCell(new Label(2, 2, res.getString(R.string.rep_quiantity), capFormat));

            WritableCellFormat tableFormat = new WritableCellFormat(
                    new WritableFont(WritableFont.ARIAL, 10));
            tableFormat.setBorder(Border.ALL, BorderLineStyle.THIN);

            WritableCellFormat tableFormatInt = new WritableCellFormat(
                    new WritableFont(WritableFont.ARIAL, 10), NumberFormats.INTEGER);
            tableFormatInt.setBorder(Border.ALL, BorderLineStyle.THIN);

            WritableCellFormat tableFormatIntB = new WritableCellFormat(
                    new WritableFont(WritableFont.ARIAL, 10, WritableFont.BOLD), NumberFormats.INTEGER);
            tableFormatIntB.setBorder(Border.ALL, BorderLineStyle.THIN);

            int idx = 3;
            int row = 1;
            for(OrderDetItem item : this.items)
            {
                sheet.addCell(new Number(0, idx, row, tableFormatInt));
                sheet.addCell(new Label(1, idx, item.goods.getName(), tableFormat));
                sheet.addCell(new Number(2, idx, item.getQty(), tableFormatInt));
                idx++;
                row++;
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

}
