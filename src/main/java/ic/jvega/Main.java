package ic.jvega;


import ic.jvega.spec.*;

import com.fasterxml.jackson.databind.JsonNode;
import ic.jvega.spec.encodings.GroupEncoding;
import ic.jvega.spec.encodings.RectEncoding;
import ic.jvega.spec.encodings.SymbolEncoding;
import ic.jvega.spec.encodings.TextEncoding;
import ic.jvega.spec.scales.BandScale;
import ic.jvega.spec.scales.LinearScale;
import ic.jvega.spec.scales.OrdinalScale;
import ic.jvega.spec.transforms.FilterTransform;
import ic.jvega.utils.GenericMap;
import ic.jvega.utils.JsonData;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;


public class Main {
    public static void main(String[] args) {

//        groupBarChartExample();
//        vegaCliExecutor("grouped_bar_test");
        scatterChartTest();
        vegaCliExecutor("scatter_chart_test");
    }

    public static void vegaCliExecutor(String filename) {
        try {
            // Check and create the 'out' directory if it doesn't exist
            File outDir = new File("./out");
            if (!outDir.exists()) {
                outDir.mkdir();
            }

            // Validate that the file exists
            File specFile = new File(outDir, filename + ".json");
            if (!specFile.exists()) {
                System.err.println("File not found: " + specFile.getAbsolutePath());
                return;
            }

            // Command to convert Vega specification to SVG
            String command = "vg2svg " + specFile.getAbsolutePath() + " > " + new File(outDir, filename + ".svg").getAbsolutePath();

            // Execute the command
            ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", command);
            Process process = processBuilder.start();

            // Read the output and error streams (for logging or debugging)
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            // Print the output (if any)
            String s;
            while ((s = stdInput.readLine()) != null) {
                System.out.println(s);
            }

            // Print any errors (if any)
            while ((s = stdError.readLine()) != null) {
                System.err.println(s);
            }

            int exitVal = process.waitFor();
            if (exitVal == 0) {
                System.out.println("Command executed successfully.");
            } else {
                System.out.println("Command execution failed with exit code: " + exitVal);
            }

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }



    public static void groupBarChartExample() {
        JsonNode groupedBarData = JsonData.readJsonFileToJsonNode("groupedBarData.json");

        VegaSpec groupedBarSpec = new VegaSpec.BuildSpec()
                .setDescription("Grouped bar")
                .setBackground("white")
                .setHeight(540)
                .setWidth(600)
                .setPadding(5)
                .setNewDataset(VegaDataset.jsonDataset("table", groupedBarData))

                .setNewScale(new BandScale.BuildScale()
                        .withName("yscale")
                        .withDomain(ScaleDomain.simpleDomain("table", "category"))
                        .withRange("height")
                        .withPadding(0.2)
                        .build())

                .setNewScale(new LinearScale.BuildScale()
                        .withName("xscale")
                        .withDomain(ScaleDomain.simpleDomain("table", "value"))
                        .withRange("width")
                        .withRound(true)
                        .build())

                .setNewScale(new OrdinalScale.BuildScale()
                        .withName("color")
                        .withDomain(ScaleDomain.simpleDomain("table", "position"))
                        .withRange(GenericMap.createMap("scheme", "category20"))
                        .build())

                .setNewAxis(new Axis.BuildAxis()
                        .setOrient("left")
                        .setScale("yscale")
                        .setTickSize(0)
                        .setLabelPadding(4)
                        .setZIndex(1)
                        .build())

                .setNewAxis(new Axis.BuildAxis()
                        .setOrient("bottom")
                        .setScale("xscale")
                        .build())

                .setNewMark(new Mark.BuildMark()
                        .withType("group")
                        .withFacetSource(Facet.simpleFacet("facet", "table", "category"))
                        .withEnter(new GroupEncoding.BuildEncoding()
                                .withY(ValueRef.ScaleField("yscale", "category"))
                                .build())
                        .withNestedSignal(new Signal.BuildSignal()
                                .withName("height")
                                .withUpdate("bandwidth('yscale')")
                                .build())
                        .withNestedScale(new BandScale.BuildScale()
                                .withName("pos")
                                .withRange("height")
                                .withDomain(ScaleDomain.simpleDomain("facet", "position"))
                                .build())
                        .withNestedMark(new Mark.BuildMark()
                                .withName("bars")
                                .withDataSource("facet")
                                .withType("rect")
                                .withEnter(new RectEncoding.BuildEncoding()
                                        .withY(ValueRef.ScaleField("pos", "position"))
                                        .withHeight(ValueRef.ScaleBand("pos", 1))
                                        .withX(ValueRef.ScaleField("xscale", "value"))
                                        .withX2(ValueRef.ScaleValue("xscale", 0))
                                        .withFill(ValueRef.ScaleField("color", "position"))
                                        .build())
                                .build())
                        .withNestedMark(new Mark.BuildMark()
                                .withType("text")
                                .withDataSource("bars")
                                .withEnter(new TextEncoding.BuildEncoding()
                                        .withAlign(ValueRef.Value("right"))
                                        .withBaseline(ValueRef.Value("middle"))
                                        .withText(ValueRef.Field("datum.value"))
                                        .withX(new ValueRef.BuildRef().withField("x2").withOffset(-5).build())
                                        .withY(new ValueRef.BuildRef()
                                                .withField("y")
                                                .withOffset(GenericMap.createMap("field", "height", "mult", 0.5))
                                                .build())
                                        .withFill(ValueRef.TestValue("contrast('white', datum.fill) > contrast('black', datum.fill)", "white"))
                                        .withFill(ValueRef.Value("black"))
                                        .build())
                                .build())
                        .build())

                .createVegaSpec();

//        System.out.println(groupedBarSpec.toJson().toPrettyString());

        JsonNode specJson = groupedBarSpec.toJson();

        String specString = specJson.toString();

        VegaSpec deserialized = VegaSpec.fromString(specString);

        String finalString = deserialized.toJson().toPrettyString();

        System.out.println("------deserialised------");

        System.out.println(finalString);

        JsonData.writeJsonNodeToFile(specJson, "grouped_bar_test.json");
    }

    public static void scatterChartTest() {
        JsonNode carsData = JsonData.readJsonFileToJsonNode("cars.json");

        Scale xScale = new LinearScale.BuildScale()
                .withName("x")
                .withRange("width")
                .withDomain(ScaleDomain.simpleDomain("source", "Horsepower"))
                .build();

        Scale yScale = new LinearScale.BuildScale()
                .withName("y")
                .withRange("height")
                .withDomain(ScaleDomain.simpleDomain("source", "Miles_per_Gallon"))
                .build();

        Scale sizeScale = new LinearScale.BuildScale()
                .withName("size")
                .withRange(Arrays.asList(4,361))
                .withDomain(ScaleDomain.simpleDomain("source", "Acceleration"))
                .build();

        Legend legend = new Legend.BuildLegend()
                .withSize("size")
                .withTitle("Acceleration")
                .withFormat("s")
                .withSymbolStrokeColor("#4682b4")
                .withSymbolStrokeWidth(2)
                .withSymbolOpacity(0.5)
                .withSymbolType("circle")
                .build();

        VegaSpec scatterSpec = new VegaSpec.BuildSpec()
                .setDescription("Scatter chart")
                .setBackground("white")
                .setWidth(700)
                .setHeight(500)
                .setPadding(5)
                .setNewDataset(new VegaDataset.BuildDataset()
                        .withName("source")
                        .withValues(carsData)
                        .withTransform(FilterTransform.simpleFilter("datum['Horsepower'] != null "
                                + "&& datum['Miles_per_Gallon'] != null "
                                + "&& datum['Acceleration'] != null"))
                        .build())
                .setNewScale(xScale)
                .setNewScale(yScale)
                .setNewScale(sizeScale)
                .setNewLegend(legend)
                .setNewAxis(new Axis.BuildAxis()
                        .setScale("x")
                        .setOrient("bottom")
                        .setGrid(true)
                        .setTickCount(5)
                        .setTitle("Horsepower")
                        .build())
                .setNewAxis(new Axis.BuildAxis()
                        .setScale("y")
                        .setOrient("left")
                        .setGrid(true)
                        .setTitle("Miles Per Gallon")
                        .setTitlePadding(5)
                        .build())
                .setNewMark(new Mark.BuildMark()
                        .withName("marks")
                        .withType("symbol")
                        .withDataSource("source")
                        .withUpdate(new SymbolEncoding.BuildEncoding()
                                .withSize(ValueRef.ScaleField("size", "Acceleration"))
                                .withX(ValueRef.ScaleField("x", "Horsepower"))
                                .withY(ValueRef.ScaleField("y", "Miles_per_Gallon"))
                                .withOpacity(ValueRef.Value(0.5))
                                .withStroke(ValueRef.Value("#4682b4"))
                                .withFill(ValueRef.Value("#4682b4"))
                                .build())
                        .build())
                .createVegaSpec();

//        System.out.println(scatterSpec.toJson().toPrettyString());

        JsonNode specJson = scatterSpec.toJson();

        String specString = specJson.toString();

        VegaSpec deserialized = VegaSpec.fromString(specString);

        String finalString = deserialized.toJson().toPrettyString();

        System.out.println("------deserialised------");

        System.out.println(finalString);
        JsonData.writeJsonNodeToFile(specJson, "scatter_chart_test.json");
    }
}