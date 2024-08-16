package com.orm.ui.fragment.map

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.orm.databinding.FragmentKakaoMapBinding

class KakaoMapFragment : Fragment() {
    private var _binding: FragmentKakaoMapBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentKakaoMapBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // MapView 초기화 및 설정
        binding.mapView.start(object : MapLifeCycleCallback() {

            override fun onMapDestroy() {
                // 지도 API가 정상적으로 종료될 때 호출됨
            }

            override fun onMapError(error: Exception) {
                // 인증 실패 및 지도 사용 중 에러가 발생할 때 호출됨
            }
        }, object : KakaoMapReadyCallback() {
            override fun onMapReady(kakaoMap: KakaoMap) {
                // 인증 후 API가 정상적으로 실행될 때 호출됨
            }

            override fun getPosition(): LatLng {
                // 지도 시작 시 위치 좌표를 설정
                return LatLng.from(37.406960, 127.115587)
            }
        })

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
