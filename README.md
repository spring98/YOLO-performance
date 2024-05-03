# 주행 중 실시간 포트홀 탐지 앱
실시간 주행 중 도로에 생긴 포트홀을 탐지하는 기존 앱의 기능 추가, 유지보수 및 코드 리팩토링 작업을 진행했습니다. 

기존 앱의 실시간 추론 성능, 발열문제, 모델 정확성 문제를 개선하기 위해 많은 고민을 하였습니다.

<br/>

해당 레포지토리는 전체코드가 아니며, 사용했던 모델 및 라이브러리의 타당성을 설명하기 위한 데모 임을 알립니다.

<br/>

## 성능 테스트

YOLO 버전, 라이브러리, 모델 사이즈 별 성능 비교

<div align="center">
  <table>
    <tr>
      <td>
        <img src="https://github.com/spring98/YOLO-performance/assets/92755385/92a68851-cfd0-4971-9b7e-b2254600b975">
        <br>
        <p align="center"> YOLOv5 (320*320) Pytorch </p>
      </td>
        <td>
        <img src="https://github.com/spring98/YOLO-performance/assets/92755385/f55a92c5-b5bb-404e-ab9b-2141377a6ed3">
        <br>
        <p align="center"> YOLOv5 (320*320) TFLite </p>
      </td>
        <td>
        <img src="https://github.com/spring98/YOLO-performance/assets/92755385/feba99ad-99cf-4667-aaaf-58abb8aa8c8c">
        <br>
        <p align="center"> YOLOv7 (640*640) TFLite </p>
      </td>
    </tr>
  </table>
</div>

<br/>

### 비교1

1번과 2번은 Pytorch_android 와 TFLite 라이브러리의 차이를 나타냅니다.

1번의 연산의 점유율은 CPU 70%대, GPU 20%대를 보이며, 평균 5프레임을 보입니다.

2번의 연산의 점유율은 CPU 50%대, GPU 50~70%대를 보이며, 평균 11프레임을 보입니다. <br/><br/>

같은 인공지능 모델을 추론 하더라도, 라이브러리 최적화에 따라 큰 성능 차이를 보이는데 이유는 다음과 같습니다.

1. 라이브러리의 하드웨어 가속 지원 여부
    
    인공지능 추론에서 하드웨어 가속을 사용하기 위해 Vulkan, Nnapi, Gpu 를 사용할 수 있는데, 공식적으로 Pytorch 에서는 Vulkan 을 지원하고, TFLite 에서는 Nnapi, Gpu 를 지원하고 있습니다.
    
    TFLite 의 Nnapi, Gpu 옵션은 잘 작동하는 반면 현재 Pytorch 의 Vulkan 옵션은 에러가 발생하여 작동하지 않습니다. 1번 영상은 CPU 옵션으로, 2번영상은 GPU 옵션을 사용했습니다. 
    
2. Cpu Architecture 차이
    
    Cpu Architecture 와 Tensorflow, Pytorch 플랫폼 간의 최적화에도 영향을 받습니다. arm 계열의 CPU 의 경우 Tensorflow 에서 성능이 더 좋다고 알려져있는데 모바일 AP의 CPU 는 arm 구조로 되어있습니다.
    
<br/>

### 비교2

2번과 3번은 같은 라이브러리 (TFLite) 일 때 모델크기에 따른 차이를 나타냅니다.

3번의 연산의 점유율은 CPU 40%대, GPU 30~50%대를 보이며, 평균 7프레임을 보입니다.

기존 모델 및 사이즈 일 때 실제 탐지되는 수준이 적절하지 않아 인공지능 개발자 분께서 새로운 640 모델을 만들어 주셨습니다. 모델의 크기가 커짐에 따라 정확성이 올라가지만 추론시 연산량이 늘어나 성능이 감소함을 알 수 있습니다.

<br/>

## 효율 테스트

장시간동안 실시간 추론이 진행되면 기기의 온도가 증가하여 성능이 저하되고 수명에도 영향을 미칩니다. 

기기의 온도, 프레임, 전력 소모량을 측정하여 가장 적절한 옵션을 찾습니다.

아래 그래프는 라이브러리, 모델 별 시간에 따른 내부 온도, 프레임 변화를 나타냅니다. 

<br/>

### 30분 추론 결과


<div align="center">
  <table>
    <tr>
      <td>
        <img src="https://github.com/spring98/YOLO-performance/assets/92755385/5b75ae0f-73d1-4c18-8b45-e20ab2170580" width="100%">
        <br>
        <p align="center"> YOLOv5 (320*320) Pytorch </p>
      </td>
      <td>
        <img src="https://github.com/spring98/YOLO-performance/assets/92755385/c9ef392a-de2f-476c-97bf-d3ef4f1627df" width="100%">
        <br>
        <p align="center"> YOLOv5 (320*320) TFLite </p>
      </td>
    </tr>
    <tr>
      <td>
        <img src="https://github.com/spring98/YOLO-performance/assets/92755385/10283824-ca73-4e03-bdea-9d12bf91ff1d" width="100%">
        <br>
        <p align="center"> YOLOv7 (640*640) TFLite </p>
      </td>
      <td>
        <img src="https://github.com/spring98/YOLO-performance/assets/92755385/0c0bddd2-7339-4fa1-885b-22a808cbb8f1" width="100%">
        <br>
        <p align="center"> YOLOv7 (640*640) TFLite Cooling </p>
      </td>
    </tr>
  </table>
</div>

<br/>

좌측상단을 1번, 우측 상단을 2번, 좌측 하단을 3번, 우측 하단을 4번 그래프라고 하고, 데이터는 안드로이드 클라이언트에서 배터리 온도 및 프레임을 계산해 로컬 서버(flask)로 전송하였습니다. 
관련 코드는 server.py 파일로 업로드 해놓았습니다.

<br/>

|  | 1번 | 2번 | 3번 | 4번 |
| --- | --- | --- | --- | --- |
| 평균 온도 | 42.20 도 | 38.36 도 | 38.68 도 | 29.23 도  |
| 평균 프레임 | 3.65 | 11.55 | 4.76 | 5.21 |

<br/>

Cooling 옵션은 같은 라이브러리에 한쪽에만 네트워크 통신 주기를 늘리고, 외부 쿨러를 설치한 결과입니다. 네트워크 통신 비용을 고려해 쌓인 데이터가 100개 이상이 될 때마다 통신을 하도록 변경하고 외부 쿨러로 조이트론의 듀얼아이스 발열쿨러를 사용하였습니다.

<br/>

<p align="center">  
  <img src="https://github.com/spring98/YOLO-performance/assets/92755385/deb69c53-7319-4868-8a2a-a84a64b73b85" align="center" width="32%">  
  <img width="10%">
  <img src="https://github.com/spring98/YOLO-performance/assets/92755385/7d894e54-f3ac-46f3-bd3b-ebe71f7c5f59" align="center" width="32%">
</p>

<br/>

### 분석 

1. 1번과 2번은 같은 모델에 라이브러리만 다르게 하여 진행한 결과로, 성능 테스트에서 알 수 있듯 CPU 만을 사용하기 때문에 효율적으로 연산하지 못해 빠르게 과열되었습니다. 1번은 2번에 비해 평균온도가 10% 높습니다.
2. 같은 조건에서 외부 쿨러를 사용한 4번은 3번에 비해 평균온도가 32% 낮고, 평균 프레임은 9% 높습니다. 냉각을 통한 열관리가 쓰로틀링으로 인한 성능감소를 부분적으로 막아줍니다.

<br/>

### 10분 전력 소모 결과

효율성을 따지기 위해서 성능분석 뿐만 아니라 전력소모 분석도 함께 이루어져야 합니다.

Android Gpu Inspector 프로그램을 이용하여 앱 사용중 하드웨어 값들을 로그로 저장해 확인할 수 있었습니다.

AGI 를 사용하여 생성되는 로그의 파일 크기가 너무 커 10분씩만 테스트 하였습니다.

<br/>

<p align="center">  
  <img src="https://github.com/spring98/YOLO-performance/assets/92755385/ddaf1375-521f-4482-b3e4-c2a3bf78a720" align="center" width="30%">
    <img width="2%">
  <img src="https://github.com/spring98/YOLO-performance/assets/92755385/464ac1e4-4142-4529-ad5f-6e58829f00fd" align="center" width="30%">
    <img width="2%">
  <img src="https://github.com/spring98/YOLO-performance/assets/92755385/4a46c6dc-90ec-4c08-8d26-d1b0e25d5981" align="center" width="30%">
</p>

<br/>

1. AGI 를 이용하여 CPU, GPU, 배터리 사용량 등을 .perfetto 파일로 저장
2. .perfetto 파일을 traceconv 프로그램으로 .txt 파일로 변환
3. 해당 텍스트 파일의 데이터는 위의 사진들과 같으며 전류 사용량은 표시한 영역과 같습니다. current_ua 의 값이 음수이면 current in 으로 충전되고 있음을 나타내며, 양수는 사용한 전류값을 나타냅니다. 단위는 마이크로 암페어입니다.
4. 에너지 사용량은 전압 * 전류 * 사용한 시간 이므로 테스트에 사용한 Galaxy S10 의 배터리 전압 3.7V * current_ua * packet time stamp 차이 를 구해서 모두 더하면 됩니다. (단위 주의) 

<br/>

### 사용된 에너지의 합

1. YOLOv5 (320*320) Pytorch: 270.67 J
2. YOLOv5 (320*320) TFLite: 11.26 J
3. YOLOv7 (640*640) TFLite: 458.90 J
4. YOLOv7 (640*640) TFLite Cooling: 785.92 J

<br/>

### 분석

1. Pytorch 보다는 TFLite 라이브러리가 GPU 를 사용하기 때문에 연산 대비 에너지 사용량이 270.67 J 에서 11.26 J 로 24배 적습니다.
2. 모델의 사이즈가 커짐에 따라 연산량이 기하급수적으로 많아져 에너지를 많이 소비하게 됩니다. 
3. 실험에 사용한 쿨러의 전력 소모를 추가적으로 고려할 때, 해당 쿨러는 5V, 1.2A 즉 6W 를 사용하므로 10분 (600초) 동안 사용하면 3600J 을 추가적으로 사용하게 됩니다. 3번에 비해 4번은 458.90 J 에서 4385.92 J 로 9.55배 더 많은 전력을 사용합니다. 9.55배 더 많은 전력을 사용하여 평균프레임 9% 를 올릴 수 있습니다.

<br/>

## 최종 결론

1. Pytorch, TFLite 라이브러리 사용에 있어 성능면에서는 평균 프레임이 3.16배, 에너지 효율면에서는 24배 차이가 나므로 TFLite 를 사용하는 것이 월등히 좋습니다.
2. 320 사이즈의 모델 정확도가 낮은 문제가 있다면 모델의 크기를 640 까지 늘려서 학습하고 사용합니다.
3. 모델의 크기가 320 에서 640 으로 커지면 성능면에서 평균프레임이 2.42배 감소하고 에너지 효율면에서는 40.75배 나빠집니다.
4. 외부 쿨러를 사용하면 성능면에서 프레임이 9% 상승하고, 에너지 효율면에서는 9.55배 나빠집니다.

<br/>

해당 프로젝트는 주행 중 도로 위 포트홀을 실시간으로 탐지하는 것입니다. 포트홀의 잘못 탐지를 막아야 했고, 최대한 높은 프레임을 유지해야 했으므로 모델의 사이즈는 가능한 크게 설정하고, 에너지 효율 보다는 성능에 초점 두기로 했습니다. 

<br/>

최종적으로, 라이브러리는 기존의 Pytorch 에서 TFLite 로 변경하고, 모델의 사이즈는 640 을 사용했으며, 쿨러를 사용하여 최대한 높은 성능을 유지하였습니다.
