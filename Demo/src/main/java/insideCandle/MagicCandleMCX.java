package insideCandle;

import static io.restassured.RestAssured.given;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.restassured.response.Response;

public class MagicCandleMCX {
	static XSSFWorkbook workbook;
	static XSSFSheet sheetSymbolList, sheetMagicCandleList, sheetTokenList;
	static FileInputStream fis;
	FileOutputStream fout;
	String kf_session, x_csrftoken;
	String forDate;

	public static void main(String[] args) throws IOException {

		MagicCandleMCX mc = new MagicCandleMCX();
		mc.stepTwo();

	}

	public void stepTwo() throws IOException {

		int rowSymbol;

		LocalDate currentDate = LocalDate.now();
		// forDate=currentDate.toString();
		forDate = "2019-04-18";
		int dom = currentDate.getDayOfMonth();
		Month m = currentDate.getMonth();

		JsonObject objOptionPE, objOptionCE, dataOptionPE, dataOptionCE;

		fis = new FileInputStream(new File("./src/test/resources/symbol_indicators.xlsx"));
		workbook = new XSSFWorkbook(fis);
		sheetSymbolList = workbook.getSheet("symbol_indicators");
		sheetTokenList = workbook.getSheet("token");
		rowSymbol = sheetSymbolList.getLastRowNum();
		Row rows;

		kf_session = sheetTokenList.getRow(1).getCell(0).toString();
		x_csrftoken = sheetTokenList.getRow(1).getCell(1).toString();

		double prvhigh, prvlow, prvopen, prvclose, prshigh, prslow, prsopen, prsclose;

		for (int row = 1; row <= rowSymbol; row++) {
			int j = 0;
			Response response = given().get("https://kitecharts-aws.zerodha.com/api/chart/"
					+ sheetSymbolList.getRow(row).getCell(2).getRawValue()
					+ "/15minute?public_token=KMq6sH0j6uCjyoInewjh70ciErD2yR4k&user_id=YG7487&api_key=kitefront&access_token=&from="
					+ forDate + "&to=" + forDate + "&ciqrandom=1555095963813").then().extract().response();

			JsonObject obj = (JsonObject) new JsonParser().parse(response.asString());
			JsonObject data = (JsonObject) obj.get("data");
			JsonArray candles = (JsonArray) data.get("candles");

			for (int i = 0; i < candles.size(); i++) {
				j++;
				if (j == candles.size()) {
					break;
				}
				JsonArray previousCandle = (JsonArray) candles.get(i);
				JsonArray presentCandle = (JsonArray) candles.get(j);

				prvopen = previousCandle.get(1).getAsDouble();
				prvhigh = previousCandle.get(2).getAsDouble();
				prvclose = previousCandle.get(4).getAsDouble();
				prvlow = previousCandle.get(3).getAsDouble();

				prsopen = presentCandle.get(1).getAsDouble();
				prshigh = presentCandle.get(2).getAsDouble();
				prsclose = presentCandle.get(4).getAsDouble();
				prslow = presentCandle.get(3).getAsDouble();

				if (prvhigh > prshigh && prvlow < prslow) {

					if (prvclose > prvopen && prsclose < prsopen || prvclose < prvopen && prsclose > prsopen) {

						if (prvhigh - prslow <= prvhigh * 1.009) {
							sheetMagicCandleList = workbook.getSheet("MagicCandle");
							int lastRow = sheetMagicCandleList.getLastRowNum();
							rows = sheetMagicCandleList.createRow(++lastRow);
							rows.createCell(0).setCellValue(presentCandle.get(0).getAsString());
							rows.createCell(1)
									.setCellValue(sheetSymbolList.getRow(row).getCell(1).getStringCellValue());
							rows.createCell(2)
									.setCellValue(sheetSymbolList.getRow(row).getCell(2).getRawValue());
							rows.createCell(3).setCellValue(prvhigh);
							rows.createCell(4).setCellValue(prvlow);
							rows.createCell(5).setCellValue(prshigh);
							rows.createCell(6).setCellValue(prslow);
							rows.createCell(9).setCellValue(prsopen);
							rows.createCell(10).setCellValue(prsclose);

							fis.close();
							FileOutputStream fos = new FileOutputStream(
									new File("./src/test/resources/symbol_indicators.xlsx"));
							workbook.write(fos);
							fos.close();
							break;

						}

					}
				}
				if (j >= 16) {
					break;
				}

			}

		}

	}

}
