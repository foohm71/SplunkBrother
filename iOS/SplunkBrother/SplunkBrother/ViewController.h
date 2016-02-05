//
//  ViewController.h
//  SplunkBrother
//
//  Created by Chris Foo on 1/28/16.
//  Copyright © 2016 Chris Foo. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <CoreLocation/CoreLocation.h>

@interface ViewController : UIViewController <CLLocationManagerDelegate>

@property (strong, nonatomic) IBOutlet UITextField *deviceIdInput;
@property (strong, nonatomic) NSString *filepath;
@property (strong, nonatomic) CLLocationManager *locationMgr;
@property (strong, nonatomic) NSString *deviceID;


- (IBAction)endTyping:(id)sender;

- (IBAction)submitButton;

- (IBAction)outsideAreaTouched:(id)sender;

-(void)splunkPing:(NSString *)deviceID withLatitude:(float)latitude withLongitude:(float)longitude;

@end

