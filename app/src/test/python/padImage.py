import cv2
import numpy as np

def main():
	input_file = './../resources/test_image/rodImage.png'
	output_file = './../resources/test_image/rodImage_pad_10_20_30_40.png'
	
	img = cv2.imread(input_file, 0)
	img = np.pad(img,((10,20),(30,40)),'constant')
	cv2.imwrite(output_file, img)
	
	input_file = './../resources/test_image/rodImage.png'
	output_file = './../resources/test_image/rodImage_pad_15_15_25_45.png'
	iteration = 3
	kernel = 5
	
	img = cv2.imread(input_file, 0)
	img = np.pad(img,((15,15),(25,45)),'constant')
	cv2.imwrite(output_file, img)

if __name__ == '__main__':
	main()
	
