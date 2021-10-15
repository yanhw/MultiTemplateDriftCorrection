import cv2

def main():
	input_file = './../resources/test_image/rodImage.png'
	output_file = './../resources/test_image/rodImage_gaussian.png'
	iteration = 3
	kernel = 5
	
	img = cv2.imread(input_file, 0)
	for i in range(iteration):
		img = cv2.GaussianBlur(img, (kernel,kernel))
	cv2.imwrite(output_file, img)


if __name__ == '__main__':
	main()
	
