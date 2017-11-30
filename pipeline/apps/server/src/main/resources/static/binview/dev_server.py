#!/usr/bin/env python

from bottle import request, response, route, run, static_file, template
import requests
import json

data = [
   {
       "_id" : "50465",
       "bin" : "50465",
       "fame" : False,
       "splash" : "splash10-0zfr-0900000000-8fc4d3f351169d801ad9",
       "binid" : 50465,
       "retentionindex" : 476055.0,
       "sampleName" : "131016ajcsa04_1",
       "kovatsRetentionIndex" : 1402.31276883052,
       "quantMass" : 158.0,
       "spectra" : "100:966.0 112:15.0 113:338.0 115:439.0 128:224.0 130:55.0 142:38.0 157:178.0 158:337.0 186:147.0 201:20.0 243:172.0 244:15.0",
       "purity" : 0.37294,
       "binUniqueMass" : 158.0
   },
   {
       "_id" : "34010",
       "bin" : "34010",
       "fame" : False,
       "splash" : "splash10-009t-0920000000-cb523ab759dc9421e364",
       "binid" : 34010,
       "retentionindex" : 588552.0,
       "sampleName" : "110922bmssa05_1",
       "kovatsRetentionIndex" : 1635.15759410951,
       "quantMass" : 174.0,
       "spectra" : "85:60.0 86:395.0 87:218.0 88:40.0 89:23.0 90:18.0 92:34.0 93:44.0 94:4.0 96:34.0 97:83.0 98:258.0 99:434.0 100:970.0 102:73.0 103:251.0 104:35.0 105:14.0 107:707.0 108:232.0 111:40.0 112:76.0 113:2085.0 114:669.0 115:133.0 119:7.0 124:6.0 125:4.0 127:26.0 128:543.0 129:2324.0 130:5413.0 131:1718.0 132:559.0 133:549.0 135:137.0 139:12.0 140:31.0 141:594.0 142:190.0 144:185.0 146:3.0 147:7899.0 148:650.0 149:505.0 150:4.0 151:10.0 152:3.0 153:35.0 154:52.0 155:177.0 156:377.0 157:246.0 158:268.0 160:96.0 161:38.0 164:57.0 168:65.0 169:37.0 170:73.0 171:3810.0 172:915.0 173:331.0 174:2357.0 175:439.0 176:88.0 178:4.0 179:121.0 182:49.0 183:11.0 184:312.0 185:62.0 186:204.0 187:727.0 188:374.0 190:541.0 191:77.0 195:11.0 196:2.0 197:5.0 199:21.0 200:1314.0 201:238.0 202:253.0 203:17.0 204:6.0 211:13.0 213:194.0 214:150.0 215:141.0 216:54.0 221:28.0 228:2237.0 229:889.0 230:244.0 231:257.0 232:624.0 233:117.0 238:1.0 240:4.0 241:30.0 245:163.0 246:140.0 247:660.0 248:180.0 249:25.0 251:29.0 256:227.0 257:4.0 258:45.0 259:146.0 260:41.0 262:344.0 263:74.0 264:25.0 270:17.0 271:5.0 274:9.0 275:6.0 276:4.0 277:151.0 278:216.0 280:15.0 281:3.0 282:23.0 285:930.0 286:138.0 290:37.0 291:4.0 292:445.0 293:104.0 294:34.0 295:2.0 296:2.0 297:8.0 299:49.0 301:105.0 302:39.0 303:85.0 304:205.0 307:96.0 308:20.0 312:8.0 316:3.0 317:4.0 328:8.0 331:20.0 332:2.0 333:421.0 334:96.0 335:47.0 338:18.0 341:3.0 343:9.0 344:103.0 345:85.0 346:59.0 348:9.0 349:66.0 350:9.0 355:25.0 363:16.0 366:15.0 368:6.0 370:8.0 371:5.0 372:1.0 373:21.0 374:2.0 377:1.0 378:23.0 383:1.0 384:1.0 388:1.0 389:6.0 390:45.0 391:9.0 398:3.0 399:2.0 402:3.0 409:5.0 417:1.0 418:165.0 419:111.0 420:21.0 428:5.0 433:45.0 434:7.0 435:10.0 436:3.0 445:2.0 449:13.0 453:8.0 462:8.0 467:4.0 470:8.0 496:4.0",
       "purity" : 0.97344,
       "binUniqueMass" : 174.0
   },
   {
       "_id" : "34011",
       "bin" : "34011",
       "fame" : False,
       "splash" : "splash10-0a4i-0490000000-8b404faf30c01de0997f",
       "binid" : 34011,
       "retentionindex" : 192313.0,
       "sampleName" : "110922bmssa05_1",
       "kovatsRetentionIndex" : 1002.747474,
       "quantMass" : 207.0,
       "spectra" : "88:133.0 91:22.0 95:120.0 100:407.0 106:8.0 109:122.0 113:63.0 114:29.0 118:222.0 120:8.0 127:387.0 128:11.0 130:1363.0 132:96.0 134:355.0 137:141.0 138:30.0 142:157.0 144:27.0 151:99.0 153:24.0 155:24.0 177:1.0 186:53.0 191:482.0 192:27.0 193:288.0 195:73.0 199:92.0 207:6165.0 208:1416.0 209:697.0 210:21.0 211:814.0 212:105.0 213:8.0 283:6.0 299:589.0 300:49.0",
       "purity" : 0.87737,
       "binUniqueMass" : 207.0
   },
   {
       "_id" : "34017",
       "bin" : "34017",
       "fame" : False,
       "splash" : "splash10-054k-0920000000-1ced0c2821d6a03464e4",
       "binid" : 34017,
       "retentionindex" : 499126.0,
       "sampleName" : "110922bmssa07_1",
       "kovatsRetentionIndex" : 1446.60048524802,
       "quantMass" : 129.0,
       "spectra" : "85:769.0 86:134.0 88:12.0 90:14.0 91:69.0 94:86.0 95:568.0 96:28.0 99:211.0 100:482.0 101:294.0 104:20.0 106:71.0 111:109.0 113:756.0 114:175.0 115:178.0 116:268.0 117:972.0 118:112.0 121:24.0 124:4.0 126:2.0 127:182.0 129:4009.0 130:283.0 131:680.0 134:55.0 139:167.0 141:8.0 143:252.0 146:32.0 147:2205.0 151:17.0 155:2359.0 156:302.0 157:1219.0 158:164.0 159:21.0 162:1.0 163:1.0 164:1.0 165:1.0 167:24.0 168:436.0 169:384.0 170:234.0 183:39.0 185:222.0 186:94.0 187:26.0 188:29.0 189:270.0 190:3.0 197:319.0 202:23.0 203:85.0 204:501.0 205:54.0 206:23.0 212:68.0 217:180.0 218:172.0 219:63.0 221:59.0 222:60.0 226:31.0 228:94.0 229:65.0 231:1.0 232:9.0 233:45.0 234:2.0 237:1.0 238:5.0 240:12.0 241:2.0 242:316.0 243:42.0 244:18.0 247:2019.0 248:436.0 249:70.0 250:12.0 253:1.0 254:48.0 256:215.0 257:637.0 258:46.0 259:166.0 261:34.0 263:2.0 272:6.0 274:13.0 278:11.0 279:1.0 285:6.0 291:4.0 293:57.0 299:44.0 301:7.0 303:17.0 306:4.0 307:2.0 310:7.0 318:35.0 349:45.0 362:5.0 367:4.0 368:5.0 377:3.0 380:5.0 384:20.0 385:12.0 390:13.0 394:7.0 408:11.0 413:1.0 419:2.0 436:2.0 438:28.0 443:12.0 445:5.0 455:17.0 466:9.0 472:32.0 475:23.0 492:6.0",
       "purity" : 0.96225,
       "binUniqueMass" : 129.0
   },
   {
       "_id" : "34013",
       "bin" : "34013",
       "fame" : False,
       "splash" : "splash10-03dj-1931000000-e5c7d70f22f3ebc6c6bc",
       "binid" : 34013,
       "retentionindex" : 672331.0,
       "sampleName" : "110922bmssa07_1",
       "kovatsRetentionIndex" : 1836.65885455848,
       "quantMass" : 313.0,
       "spectra" : "85:1185.0 86:1548.0 87:598.0 88:243.0 89:248.0 90:241.0 91:47.0 92:263.0 93:184.0 94:118.0 95:429.0 96:233.0 97:284.0 98:914.0 99:3283.0 100:946.0 101:1073.0 102:470.0 103:595.0 105:465.0 106:726.0 107:390.0 108:234.0 109:74.0 110:116.0 111:334.0 112:517.0 113:8838.0 114:1168.0 115:695.0 116:224.0 117:1781.0 118:338.0 119:220.0 120:21.0 121:123.0 122:26.0 123:136.0 124:508.0 125:32.0 126:198.0 127:491.0 128:210.0 129:2287.0 130:987.0 131:3407.0 132:530.0 133:860.0 134:427.0 135:453.0 136:356.0 137:78.0 138:162.0 139:326.0 140:1106.0 141:852.0 142:882.0 143:699.0 144:343.0 145:78.0 147:8980.0 148:1158.0 149:930.0 150:157.0 151:48.0 152:70.0 153:343.0 154:222.0 155:145.0 156:203.0 157:874.0 158:296.0 159:68.0 160:85.0 164:47.0 165:96.0 166:2253.0 167:1111.0 168:169.0 169:388.0 170:307.0 171:1764.0 172:482.0 173:336.0 174:254.0 175:282.0 176:54.0 177:79.0 178:9.0 180:16.0 181:77.0 182:21.0 183:915.0 184:246.0 185:236.0 186:12.0 187:273.0 188:34.0 189:291.0 190:179.0 191:148.0 194:46.0 195:589.0 196:339.0 197:624.0 198:119.0 199:57.0 200:174.0 201:1012.0 202:209.0 203:206.0 205:1688.0 206:284.0 207:176.0 208:17.0 209:1773.0 210:437.0 211:416.0 212:157.0 213:359.0 214:130.0 215:439.0 216:48.0 217:692.0 219:122.0 220:87.0 221:123.0 223:669.0 224:609.0 225:113.0 226:14.0 228:215.0 229:2033.0 230:481.0 231:256.0 232:28.0 234:24.0 235:22.0 236:5.0 237:18.0 238:126.0 239:35.0 240:208.0 241:2021.0 242:519.0 243:240.0 244:26.0 245:105.0 246:26.0 248:43.0 249:32.0 253:3.0 254:48.0 255:11.0 256:551.0 257:1575.0 258:344.0 259:232.0 261:2.0 266:23.0 268:16.0 269:140.0 270:70.0 272:78.0 273:198.0 274:78.0 275:49.0 276:38.0 277:1.0 278:5.0 279:2.0 284:77.0 285:384.0 286:106.0 287:33.0 288:31.0 291:79.0 292:492.0 293:114.0 294:43.0 295:150.0 297:367.0 298:107.0 303:33.0 305:6.0 306:72.0 307:62.0 308:61.0 312:8.0 313:7594.0 314:1908.0 315:724.0 316:136.0 318:5.0 319:51.0 320:8.0 321:45.0 324:1.0 327:19.0 328:326.0 329:114.0 330:20.0 331:18.0 332:337.0 333:140.0 334:63.0 345:18.0 346:21.0 347:38.0 359:75.0 360:30.0 361:61.0 364:5.0 374:38.0 378:12.0 380:18.0 389:14.0 394:55.0 401:13.0 407:12.0 408:24.0 413:16.0 415:14.0 417:16.0 423:15.0 427:5.0 428:1.0 433:54.0 435:7.0 437:18.0 448:8.0 449:4.0 450:10.0 458:3.0 464:18.0 466:10.0 468:8.0 473:4.0 475:3.0 476:5.0 478:6.0 488:10.0 496:21.0 497:2.0 500:12.0",
       "purity" : 0.76613,
       "binUniqueMass" : 313.0
   }
]

@route('/rest/bin', method = 'GET')
def bins():
    response.content_type = 'application/json'
    return json.dumps(data)

@route('/rest/bin', method = 'POST')
def bins_update():
    response.content_type = 'application/json'

    if request.forms.action == 'edit':
        k, v = [x for x in request.forms.items() if x[0].startswith('data[')][0]
        k = k.split('[')
        binid = k[1].strip(']')
        field = k[2].strip(']')

        x = [x for x in data if x['_id'] == binid][0]
        x[field] = v
        return {'data': [x]}

@route('/rest/similarity/search', method = 'POST')
def similarity_search():
    response.content_type = 'application/json'
    r=requests.post('http://mona.fiehnlab.ucdavis.edu/rest/similarity/search', data=request.body)
    return r

@route('/', method = 'GET')
def root():
    return static_file('index.html', root = './')

@route('<filename:path>', method = 'GET')
def static_content(filename):
    return static_file(filename, root = './')

if __name__ == '__main__':
    run(host = 'localhost', port = 8080, debug = True)