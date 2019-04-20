package insideCandle;

import static io.restassured.RestAssured.given;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.restassured.response.Response;

public class MagicCandle {
	static XSSFWorkbook workbook;
	static XSSFSheet sheetSymbolList, sheetMagicCandleList, sheetTokenList;
	static FileInputStream fis;
	FileOutputStream fout;
	String kf_session, x_csrftoken;
	String forDate;
	ArrayList<String> magicArray;
	int rowSymbol, rowMagic;

	public static void main(String[] args) throws IOException {

		MagicCandle mc = new MagicCandle();

		mc.stepTwo();

	}

	public void stepTwo() throws IOException {

		LocalDate currentDate = LocalDate.now();
		// forDate=currentDate.toString();
		forDate = "2019-04-18";
		int dom = currentDate.getDayOfMonth();
		Month m = currentDate.getMonth();
		magicArray = new ArrayList<String>();

		JsonObject objOptionPE, objOptionCE, dataOptionPE, dataOptionCE;

		fis = new FileInputStream(new File("./src/test/resources/symbol_indicators.xlsx"));
		workbook = new XSSFWorkbook(fis);
		sheetSymbolList = workbook.getSheet("symbol_indicators");
		sheetTokenList = workbook.getSheet("token");
		sheetMagicCandleList = workbook.getSheet("MagicCandle");

		rowSymbol = sheetSymbolList.getLastRowNum();
		rowMagic = sheetMagicCandleList.getLastRowNum();
		Row rows;

		kf_session = sheetTokenList.getRow(1).getCell(0).toString();
		x_csrftoken = sheetTokenList.getRow(1).getCell(1).toString();

		double prvhigh, prvlow, prvopen, prvclose, prshigh, prslow, prsopen, prsclose;

		for (int row = 1; row <= rowSymbol; row++) {
			
			if(sheetSymbolList.getRow(row).getCell(3).getStringCellValue().contains("Yes"))
			{
				continue;
			}
			
			int j = 0;
			Response response = given().get("https://kitecharts-aws.zerodha.com/api/chart/"
					+ sheetSymbolList.getRow(row).getCell(2).getStringCellValue()
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
									.setCellValue(sheetSymbolList.getRow(row).getCell(2).getStringCellValue());
							rows.createCell(3).setCellValue(prvhigh);
							rows.createCell(4).setCellValue(prvlow);
							rows.createCell(5).setCellValue(prshigh);
							rows.createCell(6).setCellValue(prslow);
							rows.createCell(9).setCellValue(prsopen);
							rows.createCell(10).setCellValue(prsclose);

							int rounded = 0;
							int[] lt500 = { 5, 10, 20 };

							for (int div : lt500) {

								rounded = ((int) prvhigh / div) * div;
								String pe = sheetSymbolList.getRow(row).getCell(1).getStringCellValue() + "19"
										+ m.toString().substring(0, 3).toUpperCase() + rounded + "PE";
								Response optionPE = given()
										.headers("accept", "application/json, text/plain", "content-type",
												"application/x-www-form-urlencoded", "x-csrftoken", x_csrftoken,
												"cookie",
												"__cfduid=d67619e55e581a30a763a04cc831f8a7c1555502272; kf_session="
														+ kf_session + "")
										.formParam("segment", "NFO-OPT").formParam("tradingsymbol", pe)
										.formParam("watch_id", "2021836").formParam("weight", "1")
										.post("https://kite.zerodha.com/api/marketwatch/2021836/items").then().extract()
										.response();
								objOptionPE = (JsonObject) new JsonParser().parse(optionPE.asString());
								try {
									dataOptionPE = (JsonObject) objOptionPE.get("data");
									rows.createCell(8).setCellValue(dataOptionPE.get("instrument_token").getAsString());
									break;
								} catch (Exception e) {
									// TODO Auto-generated catch block
									continue;
								}

							}

							for (int div : lt500) {
								rounded = ((int) prvhigh / div) * div;
								String ce = sheetSymbolList.getRow(row).getCell(1).getStringCellValue() + "19"
										+ m.toString().substring(0, 3).toUpperCase() + rounded + "CE";
								Response optionCE = given()
										.headers("accept", "application/json, text/plain", "content-type",
												"application/x-www-form-urlencoded", "x-csrftoken", x_csrftoken,
												"cookie",
												"__cfduid=d67619e55e581a30a763a04cc831f8a7c1555502272; kf_session="
														+ kf_session + "")
										.formParam("segment", "NFO-OPT").formParam("tradingsymbol", ce)
										.formParam("watch_id", "2021836").formParam("weight", "1").when()
										.post("https://kite.zerodha.com/api/marketwatch/2021836/items").then().extract()
										.response();

								objOptionCE = (JsonObject) new JsonParser().parse(optionCE.asString());
								try {
									dataOptionCE = (JsonObject) objOptionCE.get("data");
									rows.createCell(7).setCellValue(dataOptionCE.get("instrument_token").getAsString());
									break;

								} catch (Exception e) {
									continue;
								}
							}

							sheetSymbolList.getRow(row).createCell(3).setCellValue("Yes");
							fis.close();
							FileOutputStream fos = new FileOutputStream(
									new File("./src/test/resources/symbol_indicators.xlsx"));
							workbook.write(fos);
							fos.close();
							break;

						}

					}
				} else {
					sheetSymbolList.getRow(row).createCell(3).setCellValue("No");
					fis.close();
					FileOutputStream fos = new FileOutputStream(
							new File("./src/test/resources/symbol_indicators.xlsx"));
					workbook.write(fos);
					fos.close();
				}
				if (j >= 16) {
					break;
				}

			}

		}

	}

}
