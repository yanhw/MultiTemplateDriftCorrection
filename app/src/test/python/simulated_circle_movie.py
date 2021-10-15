import os, sys
import cv2
import numpy as np


def main():
	save_dir = './../resources/test_movie/simulated_circle_movie'
	img_size = 500
	radius = 20
	movie_length = 100
	cx = np.array(range(movie_length))
	cx = np.power(cx,2)/200.0+200
	cy = np.array(range(movie_length))
	cy = cy*0.2+100
	intensity =	np.random.rand(movie_length)*100+100
	cx = cx.astype('uint8')
	cy = cy.astype('uint8')
	intensity = intensity.astype('uint8')
	
	if (os.path.exists(save_dir) == False):
		os.mkdir(save_dir)
	for i in range(movie_length):
		img = np.zeros((img_size, img_size))
		cv2.circle(img,(cx[i],cy[i]), radius, int(intensity[i]), -1)
		output_file = os.path.join(save_dir, str(i).zfill(6)+'.png')
		cv2.imwrite(output_file, img)


if __name__ == '__main__':
	main()
	
