//
//  ViewController.m
//  SplunkBrother
//
//  Created by Chris Foo on 1/28/16.
//  Copyright © 2016 Chris Foo. All rights reserved.
//
// For more info on how to do background location checking:
// http://www.raywenderlich.com/29948/backgrounding-for-ios
// Info on making HTTP Get/Post:
// http://codewithchris.com/tutorial-how-to-use-ios-nsurlconnection-by-example/

#import "ViewController.h"

@interface ViewController ()

@end

@implementation ViewController
@synthesize deviceIdInput;
@synthesize filepath;
@synthesize locationMgr;
@synthesize deviceID;

// This is how long to sleep
float sleepInSeconds = 5;

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view, typically from a nib.
    
    // this part gets the saved deviceID and displays it
    NSFileManager *fileMgr;
    NSString *documentDir;
    NSArray *directoryPaths;
    
    fileMgr = [NSFileManager defaultManager];
    directoryPaths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
    documentDir = [directoryPaths objectAtIndex:0];
    
    filepath = [[NSString alloc] initWithString:[documentDir stringByAppendingPathComponent:@"deviceID.dat"]];
    
    if ([fileMgr fileExistsAtPath:filepath]) {
        
        deviceID = [NSKeyedUnarchiver unarchiveObjectWithFile:filepath];
        deviceIdInput.text = deviceID;
    }
    
    // this part gets the location
    locationMgr = [[CLLocationManager alloc] init];
    locationMgr.delegate = self;
    // locationMgr.distanceFilter = kCLDistanceFilterNone;
    locationMgr.desiredAccuracy = kCLLocationAccuracyBest;
    [locationMgr startUpdatingLocation];
    NSLog(@"LocationManager started");
    
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (IBAction)endTyping:(id)sender {
    [sender resignFirstResponder];
}

- (IBAction)submitButton {
    NSString *deviceID;
    
    deviceID = deviceIdInput.text;
    
    [NSKeyedArchiver archiveRootObject:deviceID toFile:filepath];
    
}

- (IBAction)outsideAreaTouched:(id)sender {
    [deviceIdInput resignFirstResponder];
}

-(void)locationManager:(CLLocationManager *)manager didFailWithError:(NSError *)error{

    [self splunkPing:deviceID withLatitude:0.0 withLongitude:0.0];
    NSLog(@"Error: %@",error.description);
}

-(void)locationManager:(CLLocationManager *)manager didUpdateLocations:(NSArray *)locations {
    // If it's a relatively recent event, turn off updates to save power.
    CLLocation* location = [locations lastObject];
    NSDate* eventDate = location.timestamp;
    NSTimeInterval howRecent = [eventDate timeIntervalSinceNow];
    NSString *coord;
    
    NSLog(@"Inside didUpdateLocations");
    
    if (fabs(howRecent) < sleepInSeconds) {
        // If the event is recent, do something with it.
        NSLog(@"latitude %+.6f, longitude %+.6f\n",
              location.coordinate.latitude,
              location.coordinate.longitude);
        coord = [[NSString alloc] initWithFormat:@"%f,%f",location.coordinate.latitude, location.coordinate.longitude];
        
        [self splunkPing:deviceID withLatitude:location.coordinate.latitude withLongitude:location.coordinate.longitude];
        
    }
}

-(void)splunkPing:(NSString *)deviceID withLatitude:(float)latitude withLongitude:(float)longitude {
    NSURL *url;
    NSString *urlstring, *httpbody;
    NSString *log = @"Ping";
    NSString *host = @"localhost";
    
    if (![deviceID  isEqual: @""]) {
        urlstring = [[NSString alloc] initWithFormat:@"http://%@/splunkrelay/devicelib/relay.php", host];
        httpbody = [[NSString alloc] initWithFormat:@"id=%@&lat=%f&long=%f&log=%@",deviceID,latitude,longitude,log];
        url = [NSURL URLWithString:urlstring];
        NSMutableURLRequest *request = [NSMutableURLRequest requestWithURL:url cachePolicy:NSURLRequestUseProtocolCachePolicy timeoutInterval:60.0];
        
        [request setHTTPMethod:@"POST"];
        [request setHTTPBody:[httpbody dataUsingEncoding:NSASCIIStringEncoding allowLossyConversion:YES]];
        
        [[NSURLConnection alloc] initWithRequest:request delegate:self];
        NSLog(@"Made call %@", urlstring);
        NSLog(@"HTTP Body %@", httpbody);
    }
}

@end
