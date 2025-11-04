START:
    A=0, C=MAR, PASSA, MAR
    A=+1, C=MBR, PASSA, MBR, WR

ADDR1:
    A=+1, C=MAR, PASSA, MAR
    A=+1, B=+1, C=MBR, ADD, MBR, WR

ADDR2:
    A=+1, B=+1, C=MAR, ADD, MAR
    A=+1, B=+1, B=+1, C=MBR, ADD, MBR, WR

LOOP:
    GOTO LOOP
