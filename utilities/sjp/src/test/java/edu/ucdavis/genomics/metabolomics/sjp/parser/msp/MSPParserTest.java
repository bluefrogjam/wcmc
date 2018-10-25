package edu.ucdavis.genomics.metabolomics.sjp.parser.msp;


import edu.ucdavis.genomics.metabolomics.sjp.Parser;
import edu.ucdavis.genomics.metabolomics.sjp.ParserHandler;
import edu.ucdavis.genomics.metabolomics.sjp.exception.ParserException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

public class MSPParserTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }


    class TestHandler implements ParserHandler {

        String spectra;
        String identifier;
        String synonyms;

        public void endAttribute(String element, String name) throws ParserException {
        }

        public void endDataSet() throws ParserException {
        }

        public void endDocument() throws ParserException {
        }

        public void endElement(String name) throws ParserException {
        }

        public void setProperties(Properties p) throws ParserException {
        }

        public void startAttribute(String element, String name, String value) throws ParserException {
        }

        public void startDataSet() throws ParserException {
        }

        public void startDocument() throws ParserException {
        }

        public void startElement(String name, String value) throws ParserException {
            if (name.equals(MSPParser.SPECTRA)) {
                spectra = value;
            }

        }

    }

    @Test
    public void testParseLine() throws ParserException {
        Parser p = new MSPParser();
        TestHandler handler = new TestHandler();

        p.parse(getClass().getResourceAsStream("/msp1.txt"), handler);
        Assert.assertTrue(handler.spectra != null);
        Assert
            .assertTrue(handler.spectra
                .trim()
                .equals(
                    "53:2 54:2 55:6 56:1 57:4 58:9 59:39 60:3 61:6 66:2 67:1 69:26 70:4 71:4 72:23 73:1000 74:83 75:71 76:4 77:4 82:1 83:1 84:1 85:4 86:1 87:6 88:6 89:24 90:2 91:1 99:5 101:48 102:7 103:384 104:36 105:18 106:1 111:1 113:6 114:1 115:18 116:87 117:208 118:21 119:16 120:2 121:1 127:2 129:61 130:8 131:38 132:6 133:105 134:14 135:8 136:1 141:5 142:1 143:13 144:1 145:4 146:2 147:535 148:80 149:51 150:5 151:3 157:1 159:2 160:1 161:4 163:6 173:2 175:14 176:2 177:6 178:1 179:1 187:1 189:161 190:30 191:135 192:23 193:12 194:2 201:1 203:20 204:189 205:365 206:75 207:36 208:5 209:2 215:12 216:3 217:622 218:122 219:54 220:8 221:24 222:5 223:3 229:1 230:4 231:25 232:5 233:3 248:2 277:34 278:10 279:5 280:1 291:5 292:1 293:56 294:16 295:9 296:1 305:17 306:8 307:144 308:41 309:20 310:4 311:1 320:24 321:8 322:4 323:1 395:3 396:1"));

    }

    @Test
    public void testParseLine6() throws ParserException {
        Parser p = new MSPParser();
        TestHandler handler = new TestHandler();

        p.parse(getClass().getResourceAsStream("/msp6.txt"), handler);
        Assert.assertTrue(handler.spectra != null);
        Assert
            .assertTrue(handler.spectra
                .trim()
                .equals(
                    "53:2 54:2 55:6 56:1 57:4 58:9 59:39 60:3 61:6 66:2 67:1 69:26 70:4 71:4 72:23 73:1000 74:83 75:71 76:4 77:4 82:1 83:1 84:1 85:4 86:1 87:6 88:6 89:24 90:2 91:1 99:5 101:48 102:7 103:384 104:36 105:18 106:1 111:1 113:6 114:1 115:18 116:87 117:208 118:21 119:16 120:2 121:1 127:2 129:61 130:8 131:38 132:6 133:105 134:14 135:8 136:1 141:5 142:1 143:13 144:1 145:4 146:2 147:535 148:80 149:51 150:5 151:3 157:1 159:2 160:1 161:4 163:6 173:2 175:14 176:2 177:6 178:1 179:1 187:1 189:161 190:30 191:135 192:23 193:12 194:2 201:1 203:20 204:189 205:365 206:75 207:36 208:5 209:2 215:12 216:3 217:622 218:122 219:54 220:8 221:24 222:5 223:3 229:1 230:4 231:25 232:5 233:3 248:2 277:34 278:10 279:5 280:1 291:5 292:1 293:56 294:16 295:9 296:1 305:17 306:8 307:144 308:41 309:20 310:4 311:1 320:24 321:8 322:4 323:1 395:3 396:1"));

    }

    @Test
    public void testParseLine5() throws ParserException {
        Parser p = new MSPParser();
        TestHandler handler = new TestHandler();

        p.parse(getClass().getResourceAsStream("/msp5.txt"), handler);
        Assert.assertTrue(handler.spectra != null);
        Assert
            .assertTrue(handler.spectra
                .trim()
                .equals(
                    "53:2 54:2 55:6 56:1 57:4 58:9 59:39 60:3 61:6 66:2 67:1 69:26 70:4 71:4 72:23 73:1000 74:83 75:71 76:4 77:4 82:1 83:1 84:1 85:4 86:1 87:6 88:6 89:24 90:2 91:1 99:5 101:48 102:7 103:384 104:36 105:18 106:1 111:1 113:6 114:1 115:18 116:87 117:208 118:21 119:16 120:2 121:1 127:2 129:61 130:8 131:38 132:6 133:105 134:14 135:8 136:1 141:5 142:1 143:13 144:1 145:4 146:2 147:535 148:80 149:51 150:5 151:3 157:1 159:2 160:1 161:4 163:6 173:2 175:14 176:2 177:6 178:1 179:1 187:1 189:161 190:30 191:135 192:23 193:12 194:2 201:1 203:20 204:189 205:365 206:75 207:36 208:5 209:2 215:12 216:3 217:622 218:122 219:54 220:8 221:24 222:5 223:3 229:1 230:4 231:25 232:5 233:3 248:2 277:34 278:10 279:5 280:1 291:5 292:1 293:56 294:16 295:9 296:1 305:17 306:8 307:144 308:41 309:20 310:4 311:1 320:24 321:8 322:4 323:1 395:3 396:1"));

    }

    @Test
    public void testParseLine2() throws ParserException {
        Parser p = new MSPParser();
        TestHandler handler = new TestHandler();

        p.parse(getClass().getResourceAsStream("/msp2.txt"), handler);
        Assert.assertTrue(handler.spectra != null);
        Assert
            .assertTrue(handler.spectra
                .trim()
                .equals(
                    "85:7 86:1 87:16 88:12 89:68 90:6 91:2 97:3 98:1 99:8 100:1 101:62 102:10 103:798 104:72 105:35 106:1 111:6 113:15 114:2 115:16 116:30 117:268 118:27 119:25 120:3 121:1 125:1 126:1 127:5 128:1 129:341 130:42 131:90 132:13 133:185 134:25 135:16 136:1 141:3 142:4 143:30 144:4 145:10 146:2 147:852 148:128 149:82 150:8 151:3 153:3 155:11 156:1 157:57 158:7 159:7 160:1 161:7 162:1 163:11 164:1 165:1 169:2 170:3 171:8 172:1 173:3 174:1 175:23 176:4 177:11 178:2 179:1 185:1 187:2 188:1 189:109 190:27 191:92 192:16 193:8 194:1 201:3 202:1 203:42 204:130 205:296 206:61 207:35 208:4 209:1 215:3 216:3 217:999 218:223 219:92 220:13 221:25 222:4 223:2 229:17 230:5 231:4 237:1 241:1 242:3 243:60 244:14 245:8 246:2 247:2 248:1 249:1 250:1 251:1 259:1 263:1 265:2 277:37 278:13 279:5 280:1 291:14 292:4 293:1 305:5 306:16 307:133 308:39 309:17 310:3 317:19 318:11 319:115 320:35 321:17 322:3 331:2 332:17 333:6 334:3 335:1 395:4 396:1 409:1 422:3 423:1"));

    }

    @Test
    public void testParseLine3() throws ParserException {
        Parser p = new MSPParser();
        TestHandler handler = new TestHandler();

        p.parse(getClass().getResourceAsStream("/msp3.txt"), handler);
        Assert.assertTrue(handler.spectra != null);
        Assert
            .assertTrue(handler.spectra
                .trim()
                .equals(
                    "85:7 86:1 87:16 88:12 89:68 90:6 91:2 97:3 98:1 99:8 100:1 101:62 102:10 103:798 104:72 105:35 106:1 111:6 113:15 114:2 115:16 116:30 117:268 118:27 119:25 120:3 121:1 125:1 126:1 127:5 128:1 129:341 130:42 131:90 132:13 133:185 134:25 135:16 136:1 141:3 142:4 143:30 144:4 145:10 146:2 147:852 148:128 149:82 150:8 151:3 153:3 155:11 156:1 157:57 158:7 159:7 160:1 161:7 162:1 163:11 164:1 165:1 169:2 170:3 171:8 172:1 173:3 174:1 175:23 176:4 177:11 178:2 179:1 185:1 187:2 188:1 189:109 190:27 191:92 192:16 193:8 194:1 201:3 202:1 203:42 204:130 205:296 206:61 207:35 208:4 209:1 215:3 216:3 217:999 218:223 219:92 220:13 221:25 222:4 223:2 229:17 230:5 231:4 237:1 241:1 242:3 243:60 244:14 245:8 246:2 247:2 248:1 249:1 250:1 251:1 259:1 263:1 265:2 277:37 278:13 279:5 280:1 291:14 292:4 293:1 305:5 306:16 307:133 308:39 309:17 310:3 317:19 318:11 319:115 320:35 321:17 322:3 331:2 332:17 333:6 334:3 335:1 395:4 396:1 409:1 422:3 423:1"));

    }

    @Test
    public void testParseLine4() throws ParserException {
        Parser p = new MSPParser();
        TestHandler handler = new TestHandler();

        p.parse(getClass().getResourceAsStream("/msp4.txt"), handler);
        Assert.assertTrue(handler.spectra != null);
        Assert
            .assertTrue(handler.spectra
                .trim()
                .equals(
                    "85:7 86:1 87:16 88:12 89:68 90:6 91:2 97:3 98:1 99:8 100:1 101:62 102:10 103:798 104:72 105:35 106:1 111:6 113:15 114:2 115:16 116:30 117:268 118:27 119:25 120:3 121:1 125:1 126:1 127:5 128:1 129:341 130:42 131:90 132:13 133:185 134:25 135:16 136:1 141:3 142:4 143:30 144:4 145:10 146:2 147:852 148:128 149:82 150:8 151:3 153:3 155:11 156:1 157:57 158:7 159:7 160:1 161:7 162:1 163:11 164:1 165:1 169:2 170:3 171:8 172:1 173:3 174:1 175:23 176:4 177:11 178:2 179:1 185:1 187:2 188:1 189:109 190:27 191:92 192:16 193:8 194:1 201:3 202:1 203:42 204:130 205:296 206:61 207:35 208:4 209:1 215:3 216:3 217:999 218:223 219:92 220:13 221:25 222:4 223:2 229:17 230:5 231:4 237:1 241:1 242:3 243:60 244:14 245:8 246:2 247:2 248:1 249:1 250:1 251:1 259:1 263:1 265:2 277:37 278:13 279:5 280:1 291:14 292:4 293:1 305:5 306:16 307:133 308:39 309:17 310:3 317:19 318:11 319:115 320:35 321:17 322:3 331:2 332:17 333:6 334:3 335:1 395:4 396:1 409:1 422:3 423:1"));

    }


    /**
     * to check that it can deal with ':' in comment fields
     *
     * @throws ParserException
     */
    @Test
    public void testParseLine7() throws ParserException {
        Parser p = new MSPParser();
        TestHandler handler = new TestHandler();

        p.parse(getClass().getResourceAsStream("/msp7.txt"), handler);
        Assert.assertTrue(handler.spectra != null);

    }
}
