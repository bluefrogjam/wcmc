package edu.ucdavis.genomics.metabolomics.sjp.parser.pegaus;


import edu.ucdavis.genomics.metabolomics.sjp.ParserHandler;
import edu.ucdavis.genomics.metabolomics.sjp.exception.ParserException;
import edu.ucdavis.genomics.metabolomics.sjp.parser.msp.MSPParser;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

public class TableMassSpecParserTest {

    class TestHandler implements ParserHandler {

        String spectra;

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

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void test() throws ParserException {
        TableMassSpecParser parser = new TableMassSpecParser();
        TestHandler handler = new TestHandler();

        parser.parse(getClass().getResourceAsStream("/table.txt"), handler);
        System.out.println(handler.spectra);
        Assert.assertTrue(handler.spectra.equals("85:999 86:14 87:55 88:127 89:0 90:21 91:0 92:0 93:0 94:40 95:111 96:0 97:356 98:213 99:262 100:33 101:15 102:85 103:19 104:22 105:23 106:6 107:0 108:0 109:117 110:118 111:87 112:0 113:53 114:73 115:0 116:0 117:0 118:44 119:0 120:0 121:0 122:109 123:41 124:67 125:80 126:0 127:111 128:101 129:0 130:123 131:168 132:165 133:0 134:198 135:0 136:8 137:0 138:0 139:45 140:0 141:32 142:0 143:89 144:52 145:11 146:61 147:179 148:80 149:0 150:81 151:0 152:0 153:224 154:152 155:0 156:19 157:0 158:61 159:74 160:49 161:0 162:24 163:0 164:4 165:0 166:0 167:6 168:0 169:4 170:0 171:8 172:152 173:0 174:0 175:0 176:24 177:0 178:9 179:0 180:0 181:0 182:17 183:0 184:143 185:51 186:51 187:17 188:28 189:77 190:0 191:0 192:0 193:0 194:8 195:0 196:13 197:4 198:9 199:11 200:20 201:35 202:45 203:0 204:41 205:0 206:0 207:0 208:0 209:0 210:0 211:5 212:60 213:45 214:23 215:0 216:0 217:81 218:11 219:3 220:0 221:4 222:0 223:0 224:0 225:18 226:0 227:1 228:11 229:13 230:13 231:8 232:0 233:96 234:33 235:0 236:10 237:5 238:20 239:0 240:104 241:15 242:0 243:0 244:6 245:7 246:7 247:20 248:4 249:0 250:0 251:0 252:0 253:18 254:25 255:0 256:0 257:0 258:12 259:34 260:7 261:7 262:0 263:0 264:0 265:0 266:57 267:0 268:116 269:25 270:2 271:5 272:0 273:9 274:2 275:28 276:18 277:12 278:0 279:0 280:0 281:0 282:0 283:35 284:0 285:20 286:10 287:0 288:0 289:14 290:15 291:20 292:7 293:0 294:5 295:0 296:0 297:0 298:0 299:0 300:0 301:2 302:0 303:7 304:0 305:30 306:17 307:7 308:3 309:2 310:6 311:18 312:8 313:16 314:0 315:2 316:7 317:9 318:7 319:23 320:27 321:6 322:1 323:0 324:0 325:0 326:0 327:0 328:0 329:33 330:23 331:0 332:0 333:25 334:9 335:3 336:0 337:0 338:0 339:0 340:0 341:0 342:0 343:0 344:0 345:0 346:13 347:1 348:0 349:0 350:1 351:0 352:0 353:0 354:0 355:0 356:0 357:0 358:3 359:19 360:14 361:16 362:3 363:189 364:66 365:12 366:10 367:0 368:1 369:2 370:0 371:1 372:0 373:0 374:1 375:7 376:0 377:0 378:0 379:0 380:0 381:0 382:0 383:0 384:0 385:5 386:0 387:46 388:33 389:1 390:11 391:23 392:0 393:0 394:0 395:3 396:0 397:0 398:5 399:2 400:0 401:37 402:58 403:4 404:3 405:11 406:0 407:0 408:0 409:4 410:0 411:2 412:0 413:0 414:6 415:2 416:0 417:17 418:69 419:30 420:2 421:0 422:0 423:0 424:0 425:0 426:4 427:0 428:0 429:0 430:0 431:0 432:0 433:0 434:0 435:0 436:0 437:1 438:0 439:0 440:0 441:8 442:0 443:20 444:15 445:347 446:180 447:57 448:5 449:9 450:0 451:0 452:0 453:2 454:0 455:0 456:2 457:0 458:0 459:11 460:57 461:26 462:0 463:0 464:0 465:0 466:2 467:0 468:4 469:0 470:0 471:0 472:6 473:0 474:0 475:0 476:0 477:3 478:0 479:0 480:0 481:0 482:0 483:0 484:0 485:0 486:1 487:0 488:0 489:0 490:0 491:0 492:0 493:0 494:0 495:0 496:0 497:0 498:0 499:0 500:0"));

    }
}
