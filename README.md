# photo-time-sort
Console tool to sort JPG files by date

```
usage: java -jar photo-time-sort.jar [-c <arg>] -d <arg> [-f <arg>] [-o <arg>] -s <arg> [-t <arg>] [-x <arg>]
 -c,--copystrategy <arg>   Copy-strategy = { DUMMY | FILE_CHANNEL | HARD_LINK | SYM_LINK }. Default = DUMMY
 -d,--dest <arg>           Dest folder
 -f,--timeformat <arg>     Comma-separated local date-time format for exif tag. Default = 'yyyy:MM:dd HH:mm:ss'
 -o,--outformat <arg>      Output folder format. Default = 'yyyy/MM/dd/HH-mm-ss'
 -s,--source <arg>         Source jpeg files folder
 -t,--trash <arg>          Unrecognized files
 -x,--exiftags <arg>       Comma-separated exif-tags list. Default = 'DateTime,DateTimeOriginal,DateTimeDigitized'
```
