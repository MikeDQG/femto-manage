import { useCallback, useEffect, useState } from 'react'
import { recognizePlace, type PlaceRecognition } from '../../api/activities'

type State = { loading: boolean; accuracy?: number; result?: PlaceRecognition; error?: string }

export function usePlaceRecognition() {
  const [state, setState] = useState<State>({ loading: false })
  const check = useCallback(() => {
    if (!navigator.geolocation) {
      setState({ loading: false, error: 'Ta brskalnik ne podpira določanja lokacije.' })
      return
    }
    setState({ loading: true })
    navigator.geolocation.getCurrentPosition(
      position => {
        void recognizePlace(position.coords.latitude, position.coords.longitude)
          .then(result => setState({ loading: false, accuracy: position.coords.accuracy, result }))
          .catch(() => setState({ loading: false, accuracy: position.coords.accuracy, error: 'Preverjanje kraja ni uspelo.' }))
      },
      error => setState({ loading: false, error: error.code === 1
        ? 'Dovoljenje za lokacijo je zavrnjeno.'
        : error.code === 2 ? 'Trenutna lokacija ni dosegljiva.' : 'Meritev lokacije ni uspela.' }),
      { enableHighAccuracy: true, timeout: 10000, maximumAge: 0 },
    )
  }, [])

  useEffect(() => {
    let active = true
    if (!navigator.permissions) return
    void navigator.permissions.query({ name: 'geolocation' }).then(permission => {
      if (active && permission.state === 'granted') check()
    }).catch(() => { /* Dovoljenje se preveri ob uporabnikovi zahtevi. */ })
    return () => { active = false }
  }, [check])

  return { ...state, check }
}

export type PlaceRecognitionState = ReturnType<typeof usePlaceRecognition>
