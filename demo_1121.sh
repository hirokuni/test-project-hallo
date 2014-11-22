adb shell am broadcast -a test.kawa.motion --es op  2_points_test_start   --ei x 300 --ei y 600 --ei x2 1400 --ei y2 600
adb shell am broadcast -a test.kawa.motion --es op  2_points_test_move_2  --ei x 300 --ei y 600 --ei x2 1400 --ei y2 600
sleep 1
adb shell am broadcast -a test.kawa.motion --es op  2_points_test_move_2  --ei x 100 --ei y 600 --ei x2 1400 --ei y2 600
sleep 1
adb shell am broadcast -a test.kawa.motion --es op  2_points_test_move_2  --ei x 300 --ei y 600 --ei x2 1400 --ei y2 600