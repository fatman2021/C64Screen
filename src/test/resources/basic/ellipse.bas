100 REM ***************
110 REM ** 			 **
120 REM ** ELLIPSE 	 **
130 REM **			 **
140 REM ***************
150 REM
160 V=53248 : SA=8192
170 GOSUB 10000 : REM GRAPHICS ON
180 CO = 1*16 + 0 : GOSUB 10400 : REM SET COLOR
190 GOSUB 10200 : REM CLEAR GRAPHICS
270 XR=100:YR=80:XM=160:YM=100:REM X/Y-RADIUS===MIDPOINT COORDINATES
280 GOSUB 11100 : REM ELLIPSE
290 WAIT 198,255 : REM WAIT FOR KEY
300 GOSUB 10600 : REM GRAPHICS OFF
310 END
320 REM
10000 REM ************************
10020 REM ** TURN ON GRAPHICS **
10040 REM ************************
10050 REm
10070 POKE V+17, PEEK(V+17) OR (8+3)*16 : REM GRAPHICS ON
10080 POKE V+22, 16 : REM MULTI-COLOR OFF
10090 POKE V+24, PBEK(V+24) OR 8 : REM GRAPHICS TO $2000(8192)
10100 RETURN
10110 REm
10200 REM *****************************
10220 REM ** CLEAR GRAPHICS SCREEN **
10240 REM *****************************
10250 REM
10270 FOR X= SA TO SA+8000 : POKE X,0: NEXT X
10300 RETURN
10310 REM
10400 REM *******************
10420 REM ** CLEAR COLOR **
10440 REM *******************
10450 REM
10460 BC = 1024 : REM BASE ADDRESS OF THE VIDEO RAM
10480 FOR X=BC TO BC+1000 : POKE X,CO : NEXT X
10510 RETURN
10520 REM
10600 REM *************************
10620 REM *? TURN OFF GRAPHICS **
10640 REM *************************
10650 REM
10670 POKE V+17, 16 : REM GRAPHICS OFF
10680 POKE V + 22, 16 : REM MULTI-COLOR OFF
10690 POKE V+24, 8 : REM CHARACTER SET BACK TO $1000 (4096)
10695 RETURN
10700 REM *********************
10720 REM ** COMPUTE POINT **
10730 REM ?* SETTING *?
10740 REM *********************
10750 REM
10760 RA = 320 * INT(YC/8) + (YC AND 7)
10770 BA = 8 * INT(XC/8)
10780 MA = 2^(7-(XC AND 7))
10790 AD = SA + RA + BA
10800 POKE AD, PEEK(AD) OR MA
10810 RETURN
11100 REM
11110 REM ********************
11120 REM ** DRAW ELLIPSE **
11130 REM ********************
11140 REM
11150 FOR F2=-1 TO 1 STEP 2 : REM RIGHT/LEFT FLAG
11160 FOR X=0 TO F2*(XR) STEP F2
11170 TC = YR * SQR(1-X^2/XR^2):XC=X+XM : REM CALCULATE POINT
11180 YC = YM + TC:GOSUB 10760:YC=YM - TC:GOSUB 10760 : REM PRINTS ABOVE/BELOW
11190 NEXT X,F2:RETURN