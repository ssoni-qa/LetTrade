package insideCandle;

import static io.restassured.RestAssured.given;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;

import org.apache.commons.math3.util.Precision;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.joda.time.DateTimeComparator;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.restassured.response.Response;

public class Breakout2 {
	FileInputStream fis;
	FileOutputStream fout;
	XSSFWorkbook workbook;
	XSSFSheet sheetBreakout, sheetMagicCandle, sheetTokenList;
	int rowBrk, rowMc;
	SimpleDateFormat sdf;
	Date timeInsideForm, currentCandleTime, breakoutTime;
	Row row;
	String kf_session, x_crfstoken;
	String forDate;
	DateTimeComparator comparator;
	int tick = 0;

	double opence, closece, openpe, closepe;

	public static void main(String[] args) throws IOException, ParseException {
		Breakout2 breakout = new Breakout2();
		breakout.check_for_breakout();

	}

	public void check_for_breakout() throws IOException, ParseException {

		double high, low;
		String time;
		fis = new FileInputStream(new File("./src/test/resources/symbol_indicators.xlsx"));
		workbook = new XSSFWorkbook(fis);
		sheetMagicCandle = workbook.getSheet("MagicCandle");
		sheetTokenList = workbook.getSheet("token");

		rowMc = sheetMagicCandle.getLastRowNum();

		kf_session = sheetTokenList.getRow(1).getCell(0).toString();
		x_crfstoken = sheetTokenList.getRow(1).getCell(1).toString();

		LocalDate currentDate = LocalDate.now();
		forDate = currentDate.toString();
		forDate = "2019-04-18";

		for (int i = 1; i <= rowMc; i++) {

			System.out.println("------------->" + sheetMagicCandle.getRow(i).getCell(1).getStringCellValue());

			if (sheetMagicCandle.getRow(i).getCell(11).getStringCellValue().contains("Yes")) {
				continue;
			}

			sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			timeInsideForm = sdf.parse(
					sheetMagicCandle.getRow(i).getCell(0).getStringCellValue().replace("+0530", "").replace("T", " "));

			Response response = given().get("https://kitecharts-aws.zerodha.com/api/chart/"
					+ sheetMagicCandle.getRow(i).getCell(2).getStringCellValue()
					+ "/minute?public_token=Sw4SgsciJmGvkBFoDZ36j83gfqjzzAFd&user_id=YG7487&api_key=kitefront&access_token=&from="
					+ forDate + "&to=" + forDate + "&ciqrandom=1555095963813").then().extract().response();

			JsonObject obj = (JsonObject) new JsonParser().parse(response.asString());
			JsonObject data = (JsonObject) obj.get("data");
			JsonArray candles = (JsonArray) data.get("candles");

			for (int i1 = 0; i1 < candles.size(); i1++) {

				JsonArray oneMinCandles = (JsonArray) candles.get(i1);
				high = oneMinCandles.get(1).getAsDouble();
				low = oneMinCandles.get(4).getAsDouble();
				time = oneMinCandles.get(0).getAsString();
				breakoutTime = sdf.parse(time.replace("+0530", "").replace("T", " "));

				comparator = DateTimeComparator.getTimeOnlyInstance();

				if (comparator.compare(timeInsideForm, breakoutTime) >= 0) {
					continue;
				}

				// Check for HIGH Break out.
				if (sheetMagicCandle.getRow(i).getCell(5).getNumericCellValue() < oneMinCandles.get(4).getAsDouble()) {

					System.out.println("I got brekout for high");

					Response responseCE = given().get("https://kitecharts-aws.zerodha.com/api/chart/"
							+ sheetMagicCandle.getRow(i).getCell(7).getStringCellValue()
							+ "/minute?public_token=KMq6sH0j6uCjyoInewjh70ciErD2yR4k&user_id=YG7487&api_key=kitefront&access_token=&from="
							+ forDate + "&to=" + forDate + "&ciqrandom=1555095963813").then().extract().response();

					JsonObject objce = (JsonObject) new JsonParser().parse(responseCE.asString());
					JsonObject datace = (JsonObject) objce.get("data");
					JsonArray candlesce = (JsonArray) datace.get("candles");

					for (int bce = 0; bce < candlesce.size(); bce++) {

						JsonArray ceCandles = (JsonArray) candlesce.get(bce);

						if (comparator.compare(timeInsideForm, sdf
								.parse(ceCandles.get(0).getAsString().replace("+0530", "").replace("T", " "))) >= 0) {
							continue;

						}

						opence = ceCandles.get(1).getAsDouble();
						closece = ceCandles.get(4).getAsDouble();
						System.out.println(
								"BC " + sdf.parse(ceCandles.get(0).getAsString().replace("+0530", "").replace("T", " "))
										+ " IC " + timeInsideForm);
						System.out.println("OPEN CE " + opence + " CLOSE CE " + closece);
						break;

					}

					Response responsePE = given().get("https://kitecharts-aws.zerodha.com/api/chart/"
							+ sheetMagicCandle.getRow(i).getCell(8).getStringCellValue()
							+ "/minute?public_token=KMq6sH0j6uCjyoInewjh70ciErD2yR4k&user_id=YG7487&api_key=kitefront&access_token=&from="
							+ forDate + "&to=" + forDate + "&ciqrandom=1555095963813").then().extract().response();

					JsonObject objpe = (JsonObject) new JsonParser().parse(responsePE.asString());
					JsonObject datape = (JsonObject) objpe.get("data");
					JsonArray candlespe = (JsonArray) datape.get("candles");

					for (int bpe = 0; bpe < candlespe.size(); bpe++) {

						JsonArray peCandle = (JsonArray) candlespe.get(bpe);

						if (comparator.compare(timeInsideForm,
								sdf.parse(peCandle.get(0).getAsString().replace("+0530", "").replace("T", " "))) >= 0) {
							continue;
						}

						openpe = peCandle.get(1).getAsDouble();
						closepe = peCandle.get(4).getAsDouble();

						System.out.println(
								"BC " + sdf.parse(peCandle.get(0).getAsString().replace("+0530", "").replace("T", " "))
										+ " IC " + timeInsideForm);
						System.out.println("OPEN PE " + openpe + " CLOSE PE " + closepe);
						break;

					}

					if (opence <= closece && openpe >= closepe) {
						sheetBreakout = workbook.getSheet("BreakoutStock");
						rowBrk = sheetBreakout.getLastRowNum();
						row = sheetBreakout.createRow(++rowBrk);
						System.out.println("BUY");
						// Time
						row.createCell(0).setCellValue(oneMinCandles.get(0).getAsString());
						// Stock Name
						row.createCell(1).setCellValue(sheetMagicCandle.getRow(i).getCell(1).getStringCellValue());
						// Stock ID
						row.createCell(2).setCellValue(sheetMagicCandle.getRow(i).getCell(2).getStringCellValue());
						// Trigger
						double trigger = Precision
								.round(sheetMagicCandle.getRow(i).getCell(5).getNumericCellValue() * 1.0004, 2);
						row.createCell(3).setCellValue(trigger);
						// Type
						row.createCell(4).setCellValue("BUY");
						// SL
						row.createCell(5).setCellValue(Precision
								.round((sheetMagicCandle.getRow(i).getCell(4).getNumericCellValue() * 0.99), 2));
						// T1
						double safeTarget = sheetMagicCandle.getRow(i).getCell(5).getNumericCellValue()
								- sheetMagicCandle.getRow(i).getCell(6).getNumericCellValue() + trigger;
						row.createCell(6).setCellValue(safeTarget);
						// T2
						row.createCell(7).setCellValue(Precision
								.round((sheetMagicCandle.getRow(i).getCell(5).getNumericCellValue() * 1.005), 2));
						// T3
						row.createCell(8).setCellValue(Precision
								.round((sheetMagicCandle.getRow(i).getCell(5).getNumericCellValue() * 1.01), 2));

						sheetMagicCandle.getRow(i).createCell(11).setCellValue("Yes");

						fis.close();
						FileOutputStream fos = new FileOutputStream(
								new File("./src/test/resources/symbol_indicators.xlsx"));
						workbook.write(fos);
						fos.close();
						break;

					}

					break;
				} else {
					sheetMagicCandle.getRow(i).createCell(11).setCellValue("No");

					fis.close();
					FileOutputStream fos = new FileOutputStream(
							new File("./src/test/resources/symbol_indicators.xlsx"));
					workbook.write(fos);
					fos.close();
				}

				// Check for low break out
				if (sheetMagicCandle.getRow(i).getCell(6).getNumericCellValue() > oneMinCandles.get(4).getAsDouble()) {

					System.out.println("I got breakout for Low");

					Response responseCE = given().get("https://kitecharts-aws.zerodha.com/api/chart/"
							+ sheetMagicCandle.getRow(i).getCell(7).getStringCellValue()
							+ "/minute?public_token=KMq6sH0j6uCjyoInewjh70ciErD2yR4k&user_id=YG7487&api_key=kitefront&access_token=&from="
							+ forDate + "&to=" + forDate + "&ciqrandom=1555095963813").then().extract().response();

					JsonObject objce = (JsonObject) new JsonParser().parse(responseCE.asString());
					JsonObject datace = (JsonObject) objce.get("data");
					JsonArray candlesce = (JsonArray) datace.get("candles");

					for (int sce = 0; sce < candlesce.size(); sce++) {

						JsonArray ceCandles = (JsonArray) candlesce.get(sce);

						if (comparator.compare(timeInsideForm, sdf
								.parse(ceCandles.get(0).getAsString().replace("+0530", "").replace("T", " "))) >= 0) {
							continue;
						}

						opence = ceCandles.get(1).getAsDouble();
						closece = ceCandles.get(4).getAsDouble();
						System.out.println(
								"BC " + sdf.parse(ceCandles.get(0).getAsString().replace("+0530", "").replace("T", " "))
										+ " IC " + timeInsideForm);
						System.out.println("OPEN CE " + opence + " CLOSE CE " + closece);
						break;

					}

					Response responsePE = given().get("https://kitecharts-aws.zerodha.com/api/chart/"
							+ sheetMagicCandle.getRow(i).getCell(8).getStringCellValue()
							+ "/minute?public_token=KMq6sH0j6uCjyoInewjh70ciErD2yR4k&user_id=YG7487&api_key=kitefront&access_token=&from="
							+ forDate + "&to=" + forDate + "&ciqrandom=1555095963813").then().extract().response();

					JsonObject objpe = (JsonObject) new JsonParser().parse(responsePE.asString());
					JsonObject datape = (JsonObject) objpe.get("data");
					JsonArray candlespe = (JsonArray) datape.get("candles");

					for (int spe = 0; spe < candlespe.size(); spe++) {

						JsonArray peCandle = (JsonArray) candlespe.get(spe);

						if (comparator.compare(timeInsideForm,
								sdf.parse(peCandle.get(0).getAsString().replace("+0530", "").replace("T", " "))) >= 0) {
							continue;
						}

						openpe = peCandle.get(1).getAsDouble();
						closepe = peCandle.get(4).getAsDouble();
						System.out.println(
								"BC " + sdf.parse(peCandle.get(0).getAsString().replace("+0530", "").replace("T", " "))
										+ " IC " + timeInsideForm);
						System.out.println("OPEN PE " + openpe + " CLOSE PE " + closepe);
						break;
					}

					if (opence >= closece && openpe <= closepe) {
						sheetBreakout = workbook.getSheet("BreakoutStock");
						rowBrk = sheetBreakout.getLastRowNum();
						row = sheetBreakout.createRow(++rowBrk);
						System.out.println("SELL");
						// Time Break Out
						row.createCell(0).setCellValue(time);
						// Stock Name
						row.createCell(1).setCellValue(sheetMagicCandle.getRow(i).getCell(1).getStringCellValue());
						// Stock ID
						row.createCell(2).setCellValue(sheetMagicCandle.getRow(i).getCell(2).getStringCellValue());
						// Trigger Price

						double trigger = Precision
								.round(sheetMagicCandle.getRow(i).getCell(5).getNumericCellValue() * 0.999, 2);
						row.createCell(3).setCellValue(trigger);
						// Position Type
						row.createCell(4).setCellValue("SELL");
						// SL
						row.createCell(5).setCellValue(Precision
								.round((sheetMagicCandle.getRow(i).getCell(3).getNumericCellValue() * 1.01), 2));
						// T1
						double safeTarget = trigger - (sheetMagicCandle.getRow(i).getCell(5).getNumericCellValue()
								- sheetMagicCandle.getRow(i).getCell(6).getNumericCellValue());
						row.createCell(6).setCellValue(safeTarget);
						// T2
						row.createCell(7).setCellValue(Precision.round((low * 0.995), 2));
						// T3
						row.createCell(8).setCellValue(Precision.round((low * 0.99), 2));
						sheetMagicCandle.getRow(i).createCell(11).setCellValue("Yes");

						fis.close();
						FileOutputStream fos = new FileOutputStream(
								new File("./src/test/resources/symbol_indicators.xlsx"));
						workbook.write(fos);
						fos.close();
						break;
					}

					break;
				} else {
					sheetMagicCandle.getRow(i).createCell(11).setCellValue("No");
					fis.close();
					FileOutputStream fos = new FileOutputStream(
							new File("./src/test/resources/symbol_indicators.xlsx"));
					workbook.write(fos);
					fos.close();
				}

			}
		}

	}

}
