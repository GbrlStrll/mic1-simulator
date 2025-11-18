START:
    A=0, C=B, PASSA, ENC
    A=+1, B=+1, B=+1, B=+1, B=+1, ADD, C=A, ENC

WRITE_LOOP:
    A=B, B=+1, C=B, ADD, ENC
    A=B, PASSA, MAR
    A=B, PASSA, MBR, WR

    A=A, B=-1, C=A, ADD, ENC

    B=A, IFZ, GOTO DONE
    GOTO WRITE_LOOP

DONE:
    GOTO DONE
